/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.NoSuchFileException
import java8.nio.file.StandardCopyOption
import me.zhanghai.android.files.provider.common.CopyOptions
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.provider.rclone.client.RcloneClient
import java.io.IOException

internal object RcloneCopyMove {
    @Throws(IOException::class)
    fun copy(source: RclonePath, target: RclonePath, copyOptions: CopyOptions) {
        if (copyOptions.atomicMove) {
            throw UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString())
        }
        val sourceItem = RcloneClient.stat(source.remoteName, source.remotePath)
            ?: throw NoSuchFileException(source.toString())
        val targetItem = RcloneClient.stat(target.remoteName, target.remotePath)
        val sourceSize = if (sourceItem.isDir) 0L else sourceItem.size.coerceAtLeast(0L)

        if (targetItem != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            if (targetItem.isDir) {
                RcloneClient.purge(target.remoteName, target.remotePath)
            } else {
                RcloneClient.deleteFile(target.remoteName, target.remotePath)
            }
        }

        if (sourceItem.isDir) {
            RcloneClient.mkdir(target.remoteName, target.remotePath)
            copyOptions.progressListener?.invoke(sourceSize)
        } else {
            try {
                RcloneClient.copyFile(
                    source.remoteName,
                    source.remotePath,
                    target.remoteName,
                    target.remotePath
                )
                copyOptions.progressListener?.invoke(sourceSize)
            } catch (e: IOException) {
                // Fall back to stream-based copy if direct copyfile is unsupported by the backend
                source.newInputStream().use { inputStream ->
                    target.newOutputStream().use { outputStream ->
                        inputStream.copyTo(
                            outputStream,
                            copyOptions.progressIntervalMillis,
                            copyOptions.progressListener
                        )
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun move(source: RclonePath, target: RclonePath, copyOptions: CopyOptions) {
        val sourceItem = RcloneClient.stat(source.remoteName, source.remotePath)
            ?: throw NoSuchFileException(source.toString())
        val targetItem = RcloneClient.stat(target.remoteName, target.remotePath)
        val sourceSize = if (sourceItem.isDir) 0L else sourceItem.size.coerceAtLeast(0L)

        if (targetItem != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            if (targetItem.isDir) {
                RcloneClient.purge(target.remoteName, target.remotePath)
            } else {
                RcloneClient.deleteFile(target.remoteName, target.remotePath)
            }
        }

        if (sourceItem.isDir) {
            try {
                RcloneClient.moveFile(
                    source.remoteName,
                    source.remotePath,
                    target.remoteName,
                    target.remotePath
                )
            } catch (e: IOException) {
                // If moving directory across remotes fails, fall back to copy and purge
                copy(source, target, copyOptions)
                RcloneClient.purge(source.remoteName, source.remotePath)
            }
        } else {
            try {
                RcloneClient.moveFile(
                    source.remoteName,
                    source.remotePath,
                    target.remoteName,
                    target.remotePath
                )
                copyOptions.progressListener?.invoke(sourceSize)
            } catch (e: IOException) {
                copy(source, target, copyOptions)
                RcloneClient.deleteFile(source.remoteName, source.remotePath)
            }
        }
    }
}
