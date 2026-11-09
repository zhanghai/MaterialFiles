/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.StrictMode
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.provider.common.getLastModifiedTime
import me.zhanghai.android.files.provider.common.size
import me.zhanghai.android.files.provider.document.documentUri
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.util.withoutPenaltyDeathOnNetwork
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI

class FileProvider : ContentProvider() {
    private lateinit var callbackThread: HandlerThread
    private lateinit var callbackHandler: Handler

    override fun onCreate(): Boolean {
        callbackThread = HandlerThread("FileProvider.CallbackThread")
        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)
        return true
    }

    override fun shutdown() {
        callbackThread.quitSafely()
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)

        if (info.exported) {
            throw SecurityException("Provider must not be exported")
        }
        if (!info.grantUriPermissions) {
            throw SecurityException("Provider must grant uri permissions")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? =
        // Strict mode thread policy is passed through binder, and many apps query the display name
        // and size on their main thread, which needs network access for remote files.
        StrictMode::class.withoutPenaltyDeathOnNetwork { queryPath(uri, projection) }

    private fun queryPath(uri: Uri, projection: Array<String?>?): Cursor {
        // ContentProvider has already checked granted permissions
        val projectionColumns = projection ?: getDefaultProjection()
        val path = uri.fileProviderPath
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        loop@ for (column in projectionColumns) {
            @Suppress("DEPRECATION")
            when (column) {
                OpenableColumns.DISPLAY_NAME -> {
                    columns += column
                    values += path.fileName.toString()
                }
                OpenableColumns.SIZE -> {
                    val size = try {
                        path.size()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                    columns += column
                    values += size
                }
                MediaStore.MediaColumns.DATA -> {
                    val file = try {
                        path.toFile()
                    } catch (e: UnsupportedOperationException) {
                        continue@loop
                    }
                    columns += column
                    values += file.absolutePath
                }
                // TODO: We should actually implement a DocumentsProvider since we are handling
                //  ACTION_OPEN_DOCUMENT.
                DocumentsContract.Document.COLUMN_MIME_TYPE -> {
                    columns += column
                    values += MimeType.guessFromPath(path.toString()).value
                }
                DocumentsContract.Document.COLUMN_LAST_MODIFIED -> {
                    val lastModified = try {
                        path.getLastModifiedTime().toMillis()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                    columns += column
                    values += lastModified
                }
            }
        }
        return MatrixCursor(columns.toTypedArray(), 1).apply {
            addRow(values)
        }
    }

    private fun getDefaultProjection(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && Binder.getCallingUid() == Process.SYSTEM_UID) {
            // com.android.internal.app.ChooserActivity.queryResolver() in Q queries with a null
            // projection (meaning all columns) on main thread but only actually needs the display
            // name (and document flags). However if we do return all the columns, we may perform
            // network requests and crash it due to StrictMode. So just work around by only
            // returning the display name in this case.
            CHOOSER_ACTIVITY_DEFAULT_PROJECTION
        } else {
            DEFAULT_PROJECTION
        }

    override fun getType(uri: Uri): String? {
        val path = uri.fileProviderPath
        return MimeType.guessFromPath(path.toString()).value
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("No external deletes")
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? =
        // ContentProvider has already checked granted permissions
        uri.fileProviderPath.openParcelFileDescriptor(mode, callbackHandler)

    companion object {
        private val DEFAULT_PROJECTION = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATA,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        private val CHOOSER_ACTIVITY_DEFAULT_PROJECTION = arrayOf(
            OpenableColumns.DISPLAY_NAME
        )
    }
}

val Path.fileProviderUri: Uri
    get() {
        // Try avoid going through FUSE two times, which is bad for media playback.
        if (isDocumentPath) {
            try {
                return documentUri
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val uriPath = Uri.encode(toUri().toString())
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
            .path(uriPath)
            .build()
    }

private val Uri.fileProviderPath: Path
    get() {
        // Strip the prepended slash. A slash is always prepended because our Uri path starts with
        // our URI scheme, which can never start with a slash; but our Uri has an authority so its
        // path must start with a slash.
        val uriPath = Uri.decode(path).substring(1)
        return Paths.get(URI.create(uriPath))
    }
