/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.system.OsConstants
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.StandardCopyOption
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.CopyOptions
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.linux.syscall.Constants
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.provider.linux.syscall.Syscalls
import java.io.IOException
import java.io.InterruptedIOException

internal object LinuxCopyMove {
    private const val SEND_FILE_COUNT = 8 * 1024

    private val XATTR_NAME_PREFIX_USER = "user.".toByteString()

    @Throws(IOException::class)
    fun copy(source: ByteString, target: ByteString, copyOptions: CopyOptions) {
        if (copyOptions.atomicMove) {
            throw UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString())
        }
        val sourceStat = try {
            if (copyOptions.noFollowLinks) Syscalls.lstat(source) else Syscalls.stat(source)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(source.toString())
        }
        val targetStat = try {
            Syscalls.lstat(target)
        } catch (e: SyscallException) {
            if (e.errno != OsConstants.ENOENT) {
                throw e.toFileSystemException(target.toString())
            }
            // Ignored.
            null
        }
        if (targetStat != null) {
            if (sourceStat.st_dev == targetStat.st_dev && sourceStat.st_ino == targetStat.st_ino) {
                copyOptions.progressListener?.invoke(sourceStat.st_size)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            // Symbolic links may not be supported so we cannot simply remove the target here.
        }
        if (OsConstants.S_ISREG(sourceStat.st_mode)) {
            if (targetStat != null) {
                try {
                    Syscalls.remove(target)
                } catch (e: SyscallException) {
                    if (e.errno != OsConstants.ENOENT) {
                        throw e.toFileSystemException(target.toString())
                    }
                }
            }
            val sourceFd = try {
                Syscalls.open(source, OsConstants.O_RDONLY, 0)
            } catch (e: SyscallException) {
                throw e.toFileSystemException(source.toString())
            }
            try {
                var targetFlags = (OsConstants.O_WRONLY or OsConstants.O_TRUNC
                    or OsConstants.O_CREAT)
                if (!copyOptions.replaceExisting) {
                    targetFlags = targetFlags or OsConstants.O_EXCL
                }
                val targetFd = try {
                    Syscalls.open(target, targetFlags, sourceStat.st_mode)
                } catch (e: SyscallException) {
                    e.maybeThrowInvalidFileNameException(target.toString())
                    throw e.toFileSystemException(target.toString())
                }
                var successful = false
                try {
                    val progressIntervalMillis = copyOptions.progressIntervalMillis
                    val progressListener = copyOptions.progressListener
                    var lastProgressMillis = System.currentTimeMillis()
                    var copiedSize = 0L
                    while (true) {
                        val sentSize = try {
                            Syscalls.sendfile(targetFd, sourceFd, null, SEND_FILE_COUNT.toLong())
                        } catch (e: SyscallException) {
                            throw e.toFileSystemException(source.toString(), target.toString())
                        }
                        if (sentSize == 0L) {
                            break
                        }
                        copiedSize += sentSize
                        throwIfInterrupted()
                        val currentTimeMillis = System.currentTimeMillis()
                        if (progressListener != null
                            && currentTimeMillis >= lastProgressMillis + progressIntervalMillis) {
                            progressListener(copiedSize)
                            lastProgressMillis = currentTimeMillis
                            copiedSize = 0
                        }
                    }
                    progressListener?.invoke(copiedSize)
                    successful = true
                } finally {
                    try {
                        Syscalls.close(targetFd)
                    } catch (e: SyscallException) {
                        throw e.toFileSystemException(target.toString())
                    } finally {
                        if (!successful) {
                            try {
                                Syscalls.remove(target)
                            } catch (e: SyscallException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } finally {
                try {
                    Syscalls.close(sourceFd)
                } catch (e: SyscallException) {
                    throw e.toFileSystemException(source.toString())
                }
            }
        } else if (OsConstants.S_ISDIR(sourceStat.st_mode)) {
            if (targetStat != null) {
                try {
                    Syscalls.remove(target)
                } catch (e: SyscallException) {
                    if (e.errno != OsConstants.ENOENT) {
                        throw e.toFileSystemException(target.toString())
                    }
                }
            }
            try {
                Syscalls.mkdir(target, sourceStat.st_mode)
            } catch (e: SyscallException) {
                e.maybeThrowInvalidFileNameException(target.toString())
                throw e.toFileSystemException(target.toString())
            }
            copyOptions.progressListener?.invoke(sourceStat.st_size)
        } else if (OsConstants.S_ISLNK(sourceStat.st_mode)) {
            val sourceTarget = try {
                Syscalls.readlink(source)
            } catch (e: SyscallException) {
                throw e.toFileSystemException(source.toString())
            }
            try {
                Syscalls.symlink(sourceTarget, target)
            } catch (e: SyscallException) {
                if (e.errno == OsConstants.EEXIST && copyOptions.replaceExisting) {
                    try {
                        Syscalls.remove(target)
                    } catch (e2: SyscallException) {
                        if (e2.errno != OsConstants.ENOENT) {
                            e2.addSuppressed(e.toFileSystemException(target.toString()))
                            throw e2.toFileSystemException(target.toString())
                        }
                    }
                    try {
                        Syscalls.symlink(sourceTarget, target)
                    } catch (e2: SyscallException) {
                        e2.addSuppressed(e.toFileSystemException(target.toString()))
                        throw e2.toFileSystemException(target.toString())
                    }
                }
                e.maybeThrowInvalidFileNameException(target.toString())
                throw e.toFileSystemException(target.toString())
            }
            copyOptions.progressListener?.invoke(sourceStat.st_size)
        } else {
            throw FileSystemException(source.toString(), null, "st_mode ${sourceStat.st_mode}")
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from now
        // on.
        // Ownership should be copied before permissions so that special permission bits like
        // setuid work properly.
        try {
            if (copyOptions.copyAttributes) {
                Syscalls.lchown(target, sourceStat.st_uid, sourceStat.st_gid)
            }
        } catch (e: SyscallException) {
            e.printStackTrace()
        }
        try {
            if (!OsConstants.S_ISLNK(sourceStat.st_mode)) {
                Syscalls.chmod(target, sourceStat.st_mode)
            }
        } catch (e: SyscallException) {
            e.printStackTrace()
        }
        // TODO: Change modified time last?
        try {
            val times = arrayOf(
                if (copyOptions.copyAttributes) {
                    sourceStat.st_atim
                } else {
                    StructTimespec(0, Constants.UTIME_OMIT)
                }, sourceStat.st_mtim
            )
            Syscalls.lutimens(target, times)
        } catch (e: SyscallException) {
            e.printStackTrace()
        }
        try {
            // TODO: Allow u+rw temporarily if we are to copy xattrs.
            val xattrNames = Syscalls.llistxattr(source)
            for (xattrName in xattrNames) {
                if (!(copyOptions.copyAttributes || xattrName.startsWith(XATTR_NAME_PREFIX_USER))) {
                    continue
                }
                val xattrValue = Syscalls.lgetxattr(target, xattrName)
                Syscalls.lsetxattr(target, xattrName, xattrValue, 0)
            }
        } catch (e: SyscallException) {
            e.printStackTrace()
        }
    }

    @Throws(InterruptedIOException::class)
    private fun throwIfInterrupted() {
        if (Thread.interrupted()) {
            throw InterruptedIOException()
        }
    }

    @Throws(IOException::class)
    fun move(source: ByteString, target: ByteString, copyOptions: CopyOptions) {
        val sourceStat = try {
            Syscalls.lstat(source)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(source.toString())
        }
        val targetStat = try {
            Syscalls.lstat(target)
        } catch (e: SyscallException) {
            if (e.errno != OsConstants.ENOENT) {
                throw e.toFileSystemException(target.toString())
            }
            // Ignored.
            null
        }
        if (targetStat != null) {
            if (sourceStat.st_dev == targetStat.st_dev && sourceStat.st_ino == targetStat.st_ino) {
                copyOptions.progressListener?.invoke(sourceStat.st_size)
                return
            }
            if (!copyOptions.replaceExisting) {
                throw FileAlreadyExistsException(source.toString(), target.toString(), null)
            }
            try {
                Syscalls.remove(target)
            } catch (e: SyscallException) {
                throw e.toFileSystemException(target.toString())
            }
        }
        var renameSuccessful = false
        try {
            Syscalls.rename(source, target)
            renameSuccessful = true
        } catch (e: SyscallException) {
            if (copyOptions.atomicMove) {
                e.maybeThrowAtomicMoveNotSupportedException(source.toString(), target.toString())
                e.maybeThrowInvalidFileNameException(target.toString())
                throw e.toFileSystemException(source.toString(), target.toString())
            }
            // Ignored.
        }
        if (renameSuccessful) {
            copyOptions.progressListener?.invoke(sourceStat.st_size)
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
            Syscalls.remove(source)
        } catch (e: SyscallException) {
            if (e.errno != OsConstants.ENOENT) {
                try {
                    Syscalls.remove(target)
                } catch (e2: SyscallException) {
                    e.addSuppressed(e2.toFileSystemException(target.toString()))
                }
            }
            throw e.toFileSystemException(source.toString())
        }
    }
}
