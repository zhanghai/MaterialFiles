/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp

import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.NoSuchFileException
import java8.nio.file.StandardCopyOption
import me.zhanghai.android.files.compat.toInstantCompat
import me.zhanghai.android.files.provider.common.CopyOptions
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.ftp.client.Client
import java.io.IOException

internal object FtpCopyMove {
    @Throws(IOException::class)
    fun copy(source: FtpPath, target: FtpPath, copyOptions: CopyOptions) {
        if (copyOptions.atomicMove) {
            throw UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString())
        }
        val sourceFile = try {
            Client.listFile(source, copyOptions.noFollowLinks)
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(source.toString())
        }
        val targetFile = try {
            Client.listFileOrNull(target, true)
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(target.toString())
        }
        val sourceSize = sourceFile.size
        if (targetFile != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            try {
                Client.delete(target, targetFile.isDirectory)
            } catch (e: IOException) {
                throw e.toFileSystemExceptionForFtp(target.toString())
            }
        }
        when {
            sourceFile.isDirectory -> {
                try {
                    Client.createDirectory(target)
                } catch (e: IOException) {
                    throw e.toFileSystemExceptionForFtp(target.toString())
                }
                copyOptions.progressListener?.invoke(sourceSize)
            }
            sourceFile.isSymbolicLink ->
                throw UnsupportedOperationException("Cannot copy symbolic links")
            else -> {
                val sourceInputStream = try {
                    Client.retrieveFile(source)
                } catch (e: IOException) {
                    throw e.toFileSystemExceptionForFtp(source.toString())
                }
                try {
                    val targetOutputStream = try {
                        Client.storeFile(target)
                    } catch (e: IOException) {
                        throw e.toFileSystemExceptionForFtp(target.toString())
                    }
                    var successful = false
                    try {
                        sourceInputStream.copyTo(
                            targetOutputStream, copyOptions.progressIntervalMillis,
                            copyOptions.progressListener
                        )
                        successful = true
                    } finally {
                        try {
                            targetOutputStream.close()
                        } catch (e: IOException) {
                            throw IOException(e).toFileSystemExceptionForFtp(target.toString())
                        } finally {
                            if (!successful) {
                                try {
                                    Client.delete(target, sourceFile.isDirectory)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } finally {
                    try {
                        sourceInputStream.close()
                    } catch (e: IOException) {
                        throw IOException(e).toFileSystemExceptionForFtp(source.toString())
                    }
                }
            }
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from now
        // on.
        if (!sourceFile.isSymbolicLink) {
            val timestamp = sourceFile.timestamp
            if (timestamp != null) {
                try {
                    Client.setLastModifiedTime(target, sourceFile.timestamp.toInstantCompat())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun move(source: FtpPath, target: FtpPath, copyOptions: CopyOptions) {
        val sourceFile = try {
            Client.listFile(source, copyOptions.noFollowLinks)
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(source.toString())
        }
        val targetFile = try {
            Client.listFileOrNull(target, true)
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(target.toString())
        }
        val sourceSize = sourceFile.size
        if (targetFile != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            try {
                Client.delete(target, targetFile.isDirectory)
            } catch (e: IOException) {
                throw e.toFileSystemExceptionForFtp(target.toString())
            }
        }
        var renameSuccessful = false
        try {
            Client.renameFile(source, target)
            renameSuccessful = true
        } catch (e: IOException) {
            if (copyOptions.atomicMove) {
                throw e.toFileSystemExceptionForFtp(source.toString(), target.toString())
            }
            // Ignored.
        }
        if (renameSuccessful) {
            copyOptions.progressListener?.invoke(sourceSize)
            return
        }
        if (copyOptions.atomicMove) {
            throw AssertionError()
        }
        var copyOptions = copyOptions
        if (!copyOptions.copyAttributes || !copyOptions.noFollowLinks) {
            copyOptions = CopyOptions(
                copyOptions.replaceExisting, true, false, true, copyOptions.progressIntervalMillis,
                copyOptions.progressListener
            )
        }
        copy(source, target, copyOptions)
        try {
            Client.delete(source, sourceFile.isDirectory)
        } catch (e: IOException) {
            if (e.toFileSystemExceptionForFtp(source.toString()) !is NoSuchFileException) {
                try {
                    Client.delete(target, sourceFile.isDirectory)
                } catch (e2: IOException) {
                    e.addSuppressed(e2.toFileSystemExceptionForFtp(target.toString()))
                }
            }
            throw e.toFileSystemExceptionForFtp(source.toString())
        }
    }
}
