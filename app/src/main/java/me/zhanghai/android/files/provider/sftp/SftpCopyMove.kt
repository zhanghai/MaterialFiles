/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp

import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.NoSuchFileException
import java8.nio.file.StandardCopyOption
import me.zhanghai.android.files.provider.common.CopyOptions
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.provider.sftp.client.Client
import me.zhanghai.android.files.provider.sftp.client.ClientException
import me.zhanghai.android.files.util.enumSetOf
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.OpenMode
import org.threeten.bp.Instant
import java.io.IOException

internal object SftpCopyMove {
    @Throws(IOException::class)
    fun copy(source: SftpPath, target: SftpPath, copyOptions: CopyOptions) {
        if (copyOptions.atomicMove) {
            throw UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString())
        }
        val sourceAttributes = try {
            if (copyOptions.noFollowLinks) Client.lstat(source) else Client.stat(source)
        } catch (e: ClientException) {
            throw e.toFileSystemException(source.toString())
        }
        if (!sourceAttributes.has(FileAttributes.Flag.MODE)) {
            throw FileSystemException(
                source.toString(), null, "Missing SSH_FILEXFER_ATTR_PERMISSIONS"
            )
        }
        val targetAttributes = try {
            Client.lstat(target)
        } catch (e: ClientException) {
            val exception = e.toFileSystemException(target.toString())
            if (exception !is NoSuchFileException) {
                throw exception
            }
            // Ignored.
            null
        }
        val sourceSize = if (sourceAttributes.has(FileAttributes.Flag.SIZE)) {
            sourceAttributes.size
        } else {
            0
        }
        if (targetAttributes != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            // Symbolic links may not be supported so we cannot simply delete the target here.
        }
        val sourceType = sourceAttributes.type
        val sourceModeAttributes = FileAttributes.Builder()
            .apply {
                if (sourceAttributes.has(FileAttributes.Flag.MODE)) {
                    withPermissions(sourceAttributes.mode.mask)
                }
            }
            .build()
        when (sourceType) {
            FileMode.Type.REGULAR -> {
                if (targetAttributes != null) {
                    try {
                        Client.remove(target)
                    } catch (e: ClientException) {
                        val exception = e.toFileSystemException(target.toString())
                        if (exception !is NoSuchFileException) {
                            throw exception
                        }
                    }
                }
                val sourceInputStream = try {
                    Client.openByteChannel(source, enumSetOf(OpenMode.READ), FileAttributes.EMPTY)
                } catch (e: ClientException) {
                    throw e.toFileSystemException(source.toString())
                }.newInputStream()
                try {
                    val targetFlags = enumSetOf(OpenMode.WRITE, OpenMode.TRUNC, OpenMode.CREAT)
                    if (!copyOptions.replaceExisting) {
                        targetFlags += OpenMode.EXCL
                    }
                    val targetOutputStream = try {
                        Client.openByteChannel(target, targetFlags, sourceModeAttributes)
                    } catch (e: ClientException) {
                        throw e.toFileSystemException(target.toString())
                    }.newOutputStream()
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
                            throw ClientException(e).toFileSystemException(target.toString())
                        } finally {
                            if (!successful) {
                                try {
                                    Client.remove(target)
                                } catch (e: ClientException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } finally {
                    try {
                        sourceInputStream.close()
                    } catch (e: IOException) {
                        throw ClientException(e).toFileSystemException(source.toString())
                    }
                }
            }
            FileMode.Type.DIRECTORY -> {
                if (targetAttributes != null) {
                    try {
                        Client.remove(target)
                    } catch (e: ClientException) {
                        val exception = e.toFileSystemException(target.toString())
                        if (exception !is NoSuchFileException) {
                            throw exception
                        }
                    }
                }
                try {
                    Client.mkdir(target, sourceModeAttributes)
                } catch (e: ClientException) {
                    throw e.toFileSystemException(target.toString())
                }
                copyOptions.progressListener?.invoke(sourceSize)
            }
            FileMode.Type.SYMLINK -> {
                val sourceTarget = try {
                    Client.readlink(source)
                } catch (e: ClientException) {
                    throw e.toFileSystemException(source.toString())
                }
                try {
                    Client.symlink(target, sourceTarget)
                } catch (e: ClientException) {
                    val exception = e.toFileSystemException(target.toString())
                    if (exception is FileAlreadyExistsException && copyOptions.replaceExisting) {
                        try {
                            Client.remove(target)
                        } catch (e2: ClientException) {
                            if (e2.toFileSystemException(target.toString())
                                    !is NoSuchFileException) {
                                e2.addSuppressed(exception)
                                throw e2.toFileSystemException(target.toString())
                            }
                        }
                        try {
                            Client.symlink(target, sourceTarget)
                        } catch (e2: ClientException) {
                            e2.addSuppressed(exception)
                            throw e2.toFileSystemException(target.toString())
                        }
                    }
                    throw e.toFileSystemException(target.toString())
                }
                copyOptions.progressListener?.invoke(sourceSize)
            }
            else -> throw FileSystemException(source.toString(), null, "type $sourceType")
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from now
        // on.
        if (sourceType != FileMode.Type.SYMLINK) {
            val attributes = FileAttributes.Builder()
                .apply {
                    if (copyOptions.copyAttributes
                        && sourceAttributes.has(FileAttributes.Flag.UIDGID)) {
                        withUIDGID(sourceAttributes.uid, sourceAttributes.gid)
                    }
                    if (sourceAttributes.type != FileMode.Type.SYMLINK
                        && sourceAttributes.has(FileAttributes.Flag.MODE)) {
                        withPermissions(sourceAttributes.mode.mask)
                    }
                    if (sourceAttributes.has(FileAttributes.Flag.ACMODTIME)) {
                        withAtimeMtime(
                            if (copyOptions.copyAttributes) {
                                sourceAttributes.atime
                            } else {
                                // We cannot leave atime unchanged in SFTP, but since we've just
                                // written the file, its atime is simply now.
                                Instant.now().epochSecond
                            }, sourceAttributes.mtime
                        )
                    }
                }
                .build()
            try {
                Client.setstat(target, attributes)
            } catch (e: ClientException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun move(source: SftpPath, target: SftpPath, copyOptions: CopyOptions) {
        val sourceAttributes = try {
            Client.lstat(source)
        } catch (e: ClientException) {
            throw e.toFileSystemException(source.toString())
        }
        if (!sourceAttributes.has(FileAttributes.Flag.MODE)) {
            throw FileSystemException(
                source.toString(), null, "Missing SSH_FILEXFER_ATTR_PERMISSIONS"
            )
        }
        val targetAttributes = try {
            Client.lstat(target)
        } catch (e: ClientException) {
            val exception = e.toFileSystemException(target.toString())
            if (exception !is NoSuchFileException) {
                throw exception
            }
            // Ignored.
            null
        }
        val sourceSize = if (sourceAttributes.has(FileAttributes.Flag.SIZE)) {
            sourceAttributes.size
        } else {
            0
        }
        if (targetAttributes != null) {
            if (source == target) {
                copyOptions.progressListener?.invoke(sourceSize)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            try {
                Client.remove(target)
            } catch (e: ClientException) {
                throw e.toFileSystemException(target.toString())
            }
        }
        var renameSuccessful = false
        try {
            Client.rename(source, target)
            renameSuccessful = true
        } catch (e: ClientException) {
            if (copyOptions.atomicMove) {
                throw e.toFileSystemException(source.toString(), target.toString())
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
            Client.remove(source)
        } catch (e: ClientException) {
            if (e.toFileSystemException(source.toString()) !is NoSuchFileException) {
                try {
                    Client.remove(target)
                } catch (e2: ClientException) {
                    e.addSuppressed(e2.toFileSystemException(target.toString()))
                }
            }
            throw e.toFileSystemException(source.toString())
        }
    }
}
