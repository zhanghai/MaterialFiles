/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall

import android.os.Build
import android.system.ErrnoException
import android.system.Int64Ref
import android.system.Os
import android.system.OsConstants
import android.system.StructPollfd
import android.system.StructStatVfs
import androidx.annotation.Size
import me.zhanghai.android.files.compat.SELinuxCompat
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.moveToByteString
import me.zhanghai.android.libselinux.SeLinux
import java.io.FileDescriptor
import java.io.InterruptedIOException

object Syscalls {
    init {
        System.loadLibrary("syscalls")
    }

    @Throws(SyscallException::class)
    external fun access(path: ByteString, mode: Int): Boolean

    @Throws(SyscallException::class)
    external fun chmod(path: ByteString, mode: Int)

    @Throws(SyscallException::class)
    external fun chown(path: ByteString, uid: Int, gid: Int)

    @Throws(SyscallException::class)
    fun close(fd: FileDescriptor) {
        try {
            Os.close(fd)
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }
    }

    @Throws(SyscallException::class)
    external fun closedir(dir: Long)

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun endgrent()

    @Throws(SyscallException::class)
    external fun endmntent(file: Long)

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun endpwent()

    private external fun errno(): Int

    @Throws(SyscallException::class)
    fun fcntl(fd: FileDescriptor, cmd: Int): Int = fcntl_void(fd, cmd)

    @Throws(SyscallException::class)
    fun fcntl(fd: FileDescriptor, cmd: Int, arg: Int): Int = fcntl_int(fd, cmd, arg)

    @Throws(SyscallException::class)
    private external fun fcntl_int(fd: FileDescriptor, cmd: Int, arg: Int): Int

    @Throws(SyscallException::class)
    private external fun fcntl_void(fd: FileDescriptor, cmd: Int): Int

    @Throws(SyscallException::class)
    fun getfilecon(path: ByteString): ByteString =
        try {
            SeLinux.getfilecon(path.borrowBytes()).moveToByteString()
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun getgrent(): StructGroup?

    @Throws(SyscallException::class)
    external fun getgrgid(gid: Int): StructGroup?

    @Throws(SyscallException::class)
    external fun getgrnam(name: ByteString): StructGroup?

    @Throws(SyscallException::class)
    external fun getmntent(file: Long): StructMntent?

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun getpwent(): StructPasswd?

    @Throws(SyscallException::class)
    external fun getpwnam(name: ByteString): StructPasswd?

    @Throws(SyscallException::class)
    external fun getpwuid(uid: Int): StructPasswd?

    external fun hasmntopt(mntent: StructMntent, option: ByteString): Boolean

    @Throws(SyscallException::class)
    external fun inotify_add_watch(fd: FileDescriptor, path: ByteString, mask: Int): Int

    @Throws(SyscallException::class)
    external fun inotify_init1(flags: Int): FileDescriptor

    @Throws(SyscallException::class)
    external fun inotify_get_events(
        buffer: ByteArray,
        offset: Int,
        length: Int
    ): Array<StructInotifyEvent>

    @Throws(SyscallException::class)
    external fun inotify_rm_watch(fd: FileDescriptor, wd: Int)

    @Throws(SyscallException::class)
    external fun ioctl_int(fd: FileDescriptor, request: Int, argument: Int32Ref?): Int

    fun is_selinux_enabled(): Boolean = SeLinux.is_selinux_enabled()

    @Throws(SyscallException::class)
    external fun lchown(path: ByteString, uid: Int, gid: Int)

    @Throws(SyscallException::class)
    fun lgetfilecon(path: ByteString): ByteString =
        try {
            SeLinux.lgetfilecon(path.borrowBytes()).moveToByteString()
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }

    @Throws(SyscallException::class)
    fun lsetfilecon(path: ByteString, context: ByteString) {
        try {
            SeLinux.lsetfilecon(path.borrowBytes(), context.borrowBytes())
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }
    }

    @Throws(SyscallException::class)
    external fun lgetxattr(path: ByteString, name: ByteString): ByteArray

    @Throws(SyscallException::class)
    external fun link(oldPath: ByteString, newPath: ByteString)

    @Throws(SyscallException::class)
    external fun llistxattr(path: ByteString): Array<ByteString>

    @Throws(SyscallException::class)
    external fun lsetxattr(path: ByteString, name: ByteString, value: ByteArray, flags: Int)

    @Throws(SyscallException::class)
    external fun lstat(path: ByteString): StructStat

    @Throws(SyscallException::class)
    external fun lutimens(path: ByteString, @Size(2) times: Array<StructTimespec>)

    @Throws(SyscallException::class)
    external fun mkdir(path: ByteString, mode: Int)

    @Throws(SyscallException::class)
    external fun mount(
        source: ByteString?,
        target: ByteString,
        fileSystemType: ByteString?,
        mountFlags: Long,
        data: ByteArray?
    ): Int

    @Throws(SyscallException::class)
    external fun open(path: ByteString, flags: Int, mode: Int): FileDescriptor

    @Throws(SyscallException::class)
    external fun opendir(path: ByteString): Long

    @Throws(SyscallException::class)
    fun poll(fds: Array<StructPollfd>, timeout: Int): Int =
        try {
            Os_poll(fds, timeout)
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }

    @Throws(ErrnoException::class)
    private fun Os_poll(fds: Array<StructPollfd>, timeout: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || timeout < 0) {
            return Os.poll(fds, timeout)
        } else {
            val timeoutTime = System.currentTimeMillis() + timeout
            var timeout = timeout
            while (true) {
                return try {
                    Os.poll(fds, timeout)
                } catch (e: ErrnoException) {
                    if (e.errno == OsConstants.EINTR) {
                        val newTimeout = timeoutTime - System.currentTimeMillis()
                        if (newTimeout <= 0) {
                            return 0
                        }
                        timeout = newTimeout.toInt()
                        continue
                    }
                    throw e
                }
            }
        }
    }

    @Throws(InterruptedIOException::class, SyscallException::class)
    fun read(
        fd: FileDescriptor,
        buffer: ByteArray,
        offset: Int = 0,
        length: Int = buffer.size
    ): Int =
        try {
            Os.read(fd, buffer, offset, length)
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }

    @Throws(SyscallException::class)
    external fun readdir(dir: Long): StructDirent?

    @Throws(SyscallException::class)
    external fun readlink(path: ByteString): ByteString

    @Throws(SyscallException::class)
    external fun realpath(path: ByteString): ByteString

    @Throws(SyscallException::class)
    external fun remove(path: ByteString)

    @Throws(SyscallException::class)
    external fun rename(oldPath: ByteString, newPath: ByteString)

    @Throws(SyscallException::class)
    fun security_getenforce(): Boolean =
        try {
            SeLinux.security_getenforce()
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }

    @Throws(SyscallException::class)
    fun selinux_android_restorecon(path: ByteString, flags: Int) {
        // FIXME: Platform SELinux class cannot accept byte array, so we have to use a string.
        val successful = SELinuxCompat.native_restorecon(path.toString(), flags)
        if (!successful) {
            var errno = errno()
            if (errno == 0) {
                // Just set some generic error.
                errno = OsConstants.EIO
            }
            throw SyscallException("selinux_android_restorecon", errno)
        }
    }

    @Throws(SyscallException::class)
    external fun sendfile(
        outFd: FileDescriptor,
        inFd: FileDescriptor,
        offset: Int64Ref?
        , count: Long
    ): Long

    @Throws(SyscallException::class)
    fun setfilecon(path: ByteString, context: ByteString) {
        try {
            SeLinux.setfilecon(path.borrowBytes(), context.borrowBytes())
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }
    }

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun setgrent()

    @Throws(SyscallException::class)
    external fun setmntent(path: ByteString, mode: ByteString): Long

    @kotlin.jvm.JvmStatic
    @Throws(SyscallException::class)
    external fun setpwent()

    @Size(2)
    @Throws(SyscallException::class)
    fun socketpair(domain: Int, type: Int, protocol: Int): Array<FileDescriptor> {
        val fds = arrayOf(FileDescriptor(), FileDescriptor())
        try {
            Os.socketpair(domain, type, protocol, fds[0], fds[1])
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }
        return fds
    }

    @Throws(SyscallException::class)
    external fun stat(path: ByteString): StructStat

    @Throws(SyscallException::class)
    external fun statvfs(path: ByteString): StructStatVfs

    fun strerror(errno: Int): String = Os.strerror(errno)

    @Throws(SyscallException::class)
    external fun symlink(target: ByteString, linkPath: ByteString)

    @Throws(SyscallException::class)
    external fun utimens(path: ByteString, @Size(2) times: Array<StructTimespec>)

    @Throws(InterruptedIOException::class, SyscallException::class)
    fun write(
        fd: FileDescriptor,
        buffer: ByteArray,
        offset: Int = 0,
        length: Int = buffer.size
    ): Int =
        try {
            Os.write(fd, buffer, offset, length)
        } catch (e: ErrnoException) {
            throw SyscallException(e)
        }
}
