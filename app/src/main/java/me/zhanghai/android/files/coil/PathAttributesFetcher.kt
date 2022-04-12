/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import androidx.core.graphics.drawable.toDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.fetch.VideoFrameFetcher
import coil.size.PixelSize
import coil.size.Size
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.use
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asMimeType
import me.zhanghai.android.files.file.isApk
import me.zhanghai.android.files.file.isImage
import me.zhanghai.android.files.file.isMedia
import me.zhanghai.android.files.file.isPdf
import me.zhanghai.android.files.file.isVideo
import me.zhanghai.android.files.file.lastModifiedInstant
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.content.resolver.ResolverException
import me.zhanghai.android.files.provider.document.documentSupportsThumbnail
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.provider.ftp.isFtpPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.getDimensionPixelSize
import me.zhanghai.android.files.util.getPackageArchiveInfoCompat
import me.zhanghai.android.files.util.isGetPackageArchiveInfoCompatible
import me.zhanghai.android.files.util.runWithCancellationSignal
import me.zhanghai.android.files.util.setDataSource
import me.zhanghai.android.files.util.valueCompat
import okio.buffer
import okio.source
import java.io.Closeable
import java.io.IOException
import me.zhanghai.android.files.util.setDataSource as appSetDataSource

class PathAttributesFetcher(
    private val context: Context
) : Fetcher<Pair<Path, BasicFileAttributes>> {
    private val appIconFetcher = object : AppIconFetcher<Path>(
        // This is used by FileListAdapter.
        context.getDimensionPixelSize(R.dimen.large_icon_size), context
    ) {
        override fun key(data: Path): String? {
            throw AssertionError(data)
        }

        override fun getApplicationInfo(data: Path): Pair<ApplicationInfo, Closeable?> {
            val (packageInfo, closeable) =
                context.packageManager.getPackageArchiveInfoCompat(data, 0)
            val applicationInfo = packageInfo?.applicationInfo
            if (applicationInfo == null) {
                closeable?.close()
                throw IOException("ApplicationInfo is null")
            }
            return applicationInfo to closeable
        }
    }

    private val videoFrameFetcher = object : VideoFrameFetcher<Path>(context) {
        override fun key(data: Path): String? {
            throw AssertionError(data)
        }

        override fun MediaMetadataRetriever.setDataSource(data: Path) {
            appSetDataSource(data)
        }
    }

    private val pdfPageFetcher = object : PdfPageFetcher<Path>(context) {
        override fun key(data: Path): String? {
            throw AssertionError(data)
        }

        override fun openParcelFileDescriptor(data: Path): ParcelFileDescriptor =
            when {
                data.isLinuxPath ->
                    ParcelFileDescriptor.open(data.toFile(), ParcelFileDescriptor.MODE_READ_ONLY)
                data.isDocumentPath ->
                    DocumentResolver.openParcelFileDescriptor(data as DocumentResolver.Path, "r")
                else -> throw IllegalArgumentException(data.toString())
            }
    }

    override fun key(data: Pair<Path, BasicFileAttributes>): String {
        val (path, attributes) = data
        return "$path:${attributes.lastModifiedInstant.toEpochMilli()}"
    }

    override suspend fun fetch(
        pool: BitmapPool,
        data: Pair<Path, BasicFileAttributes>,
        size: Size,
        options: Options
    ): FetchResult {
        val (path, attributes) = data
        // @see android.provider.MediaStore.ThumbnailConstants.MINI_SIZE
        val isThumbnail = size is PixelSize && size.width <= 512 && size.height <= 384
        if (isThumbnail) {
            if (path.isDocumentPath && attributes.documentSupportsThumbnail) {
                size as PixelSize
                val thumbnail = runWithCancellationSignal { signal ->
                    try {
                        DocumentResolver.getThumbnail(
                            path as DocumentResolver.Path, size.width, size.height, signal
                        )
                    } catch (e: ResolverException) {
                        e.printStackTrace()
                        null
                    }
                }
                if (thumbnail != null) {
                    return DrawableResult(
                        thumbnail.toDrawable(context.resources), true, DataSource.DISK
                    )
                }
            }
            val isLocalPath = path.isLinuxPath
                || (path.isDocumentPath && DocumentResolver.isLocal(path as DocumentResolver.Path))
            // FTP doesn't support random access and requires one connection per parallel read.
            val shouldReadRemotePath = !path.isFtpPath
                && Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.valueCompat
            if (!(isLocalPath || shouldReadRemotePath)) {
                error("Cannot read $path for thumbnail")
            }
        }
        val mimeType = AndroidFileTypeDetector.getMimeType(data.first, data.second).asMimeType()
        when {
            mimeType.isApk && path.isGetPackageArchiveInfoCompatible -> {
                try {
                    return appIconFetcher.fetch(pool, path, size, options)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mimeType.isImage || mimeType == MimeType.GENERIC -> {
                val inputStream = path.newInputStream()
                return SourceResult(
                    inputStream.source().buffer(),
                    if (mimeType != MimeType.GENERIC) mimeType.value else null,
                    DataSource.DISK
                )
            }
            mimeType.isMedia && (path.isLinuxPath || path.isDocumentPath) -> {
                val embeddedPicture = try {
                    MediaMetadataRetriever().use { retriever ->
                        retriever.setDataSource(path)
                        retriever.embeddedPicture
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (embeddedPicture != null) {
                    return SourceResult(
                        embeddedPicture.inputStream().source().buffer(), null, DataSource.DISK
                    )
                }
                if (mimeType.isVideo) {
                    try {
                        return videoFrameFetcher.fetch(pool, path, size, options)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            mimeType.isPdf && (path.isLinuxPath || path.isDocumentPath) -> {
                try {
                    return pdfPageFetcher.fetch(pool, path, size, options)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        error("Cannot fetch $path")
    }
}
