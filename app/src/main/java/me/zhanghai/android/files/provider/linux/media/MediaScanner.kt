/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.media

import android.media.MediaScannerConnection
import android.mtp.MtpConstants
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java8.nio.channels.FileChannel
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.contentResolver
import me.zhanghai.android.files.hiddenapi.RestrictedHiddenApi
import me.zhanghai.android.files.provider.common.DelegateFileChannel
import me.zhanghai.android.files.provider.root.isRunningAsRoot
import me.zhanghai.android.files.util.lazyReflectedMethod
import java.io.File
import java.io.IOException

/*
 * @see com.android.internal.content.FileSystemProvider
 * @see com.android.providers.media.scan.ModernMediaScanner.java
 */
object MediaScanner {
    fun scan(file: File, isDeleted: Boolean = false) {
        if (isRunningAsRoot) {
            return
        }
        MediaScannerConnection.scanFile(application, arrayOf(file.path), null) { _, _ ->
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && isDeleted) {
                // ModernMediaScanner has a bug on Android 10 that may prevent it from removing
                // certain files after their deletion. This has been fixed on Android 11 by
                // https://android.googlesource.com/platform/packages/providers/MediaProvider/+/637d133d90f49dd18bda5de219184bfa9d6c2deb
                // , but we still have to work around it for Android 10 by always trying to delete
                // the MediaStore entry ourselves.
                deleteMediaStoreEntryAsync(file)
            }
        }
    }

    @get:RequiresApi(Build.VERSION_CODES.Q)
    private val deleteMediaStoreEntryHandler by lazy {
        val thread = HandlerThread("DeleteMediaStoreEntry")
        thread.start()
        Handler(thread.looper)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun deleteMediaStoreEntryAsync(file: File) {
        deleteMediaStoreEntryHandler.post {
            try {
                deleteMediaStoreEntrySync(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RestrictedHiddenApi
    @get:RequiresApi(Build.VERSION_CODES.Q)
    private val mediaStoreGetVolumeName by lazyReflectedMethod(
        MediaStore::class.java, "getVolumeName", File::class.java
    )

    // @see com.android.providers.media.scan.ModernMediaScanner.reconcileAndClean
    // @see https://android.googlesource.com/platform/packages/providers/MediaProvider/+/android10-release/src/com/android/providers/media/scan/ModernMediaScanner.java
    // @see https://android.googlesource.com/platform/packages/providers/MediaProvider/+/android11-release/src/com/android/providers/media/scan/ModernMediaScanner.java
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun deleteMediaStoreEntrySync(file: File) {
        val file = file.canonicalFile
        val volumeName = mediaStoreGetVolumeName.invoke(null, file) as String
        val uri = MediaStore.Files.getContentUri(volumeName)
            .buildUpon()
            .appendQueryParameter("includePending", "1")
            .appendQueryParameter("deletedata", "false")
            .build()
        @Suppress("DEPRECATION")
        val where = "ifnull(format, ${MtpConstants.FORMAT_UNDEFINED}) != ${
            MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST} AND ${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        contentResolver.delete(uri, where, selectionArgs)
    }

    fun createScanOnCloseFileChannel(fileChannel: FileChannel, file: File): FileChannel =
        if (isRunningAsRoot) {
            fileChannel
        } else {
            object : DelegateFileChannel(fileChannel) {
                @Throws(IOException::class)
                override fun implCloseChannel() {
                    super.implCloseChannel()

                    scan(file)
                }
            }
        }
}
