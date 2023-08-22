/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.system.OsConstants
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.PosixFileAttributeView
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixUser
import me.zhanghai.android.files.provider.common.toInt
import me.zhanghai.android.files.provider.linux.syscall.Constants
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.provider.linux.syscall.Syscall
import java.io.IOException

internal class LocalLinuxFileAttributeView(
    private val path: ByteString,
    private val noFollowLinks: Boolean
) : PosixFileAttributeView {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): LinuxFileAttributes {
        val stat = try {
            if (noFollowLinks) {
                Syscall.lstat(path)
            } else {
                Syscall.stat(path)
            }
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
        val owner = try {
            LinuxUserPrincipalLookupService.getUserById(stat.st_uid)
        } catch (e: SyscallException) {
            // It's okay to have a non-existent UID.
            e.toFileSystemException(path.toString()).printStackTrace()
            PosixUser(stat.st_uid, null)
        }
        val group = try {
            LinuxUserPrincipalLookupService.getGroupById(stat.st_gid)
        } catch (e: SyscallException) {
            // It's okay to have a non-existent GID.
            e.toFileSystemException(path.toString()).printStackTrace()
            PosixGroup(stat.st_gid, null)
        }
        val seLinuxContext = try {
            if (noFollowLinks) {
                Syscall.lgetfilecon(path)
            } else {
                Syscall.getfilecon(path)
            }
        } catch (e: SyscallException) {
            // SELinux calls may fail with ENODATA or ENOTSUP, and there may be other errors.
            e.toFileSystemException(path.toString()).printStackTrace()
            if (e.errno == OsConstants.ENODATA) ByteString.EMPTY else null
        }
        return LinuxFileAttributes.from(stat, owner, group, seLinuxContext)
    }

    @Throws(IOException::class)
    override fun setTimes(
        lastModifiedTime: FileTime?,
        lastAccessTime: FileTime?,
        createTime: FileTime?
    ) {
        if (lastAccessTime == null && lastModifiedTime == null) {
            // Only throw if caller is trying to set only create time, so that foreign copy move can
            // still set other times.
            if (createTime != null) {
                throw UnsupportedOperationException("createTime")
            }
            return
        }
        val times = arrayOf(lastAccessTime.toTimespec(), lastModifiedTime.toTimespec())
        try {
            if (noFollowLinks) {
                Syscall.lutimens(path, times)
            } else {
                Syscall.utimens(path, times)
            }
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    private fun FileTime?.toTimespec(): StructTimespec {
        if (this == null) {
            return StructTimespec(0, Constants.UTIME_OMIT)
        }
        val instant = toInstant()
        return StructTimespec(instant.epochSecond, instant.nano.toLong())
    }

    @Throws(IOException::class)
    override fun setOwner(owner: PosixUser) {
        val uid = owner.id
        try {
            if (noFollowLinks) {
                Syscall.lchown(path, uid, -1)
            } else {
                Syscall.chown(path, uid, -1)
            }
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    @Throws(IOException::class)
    override fun setGroup(group: PosixGroup) {
        val gid = group.id
        try {
            if (noFollowLinks) {
                Syscall.lchown(path, -1, gid)
            } else {
                Syscall.chown(path, -1, gid)
            }
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    @Throws(IOException::class)
    override fun setMode(mode: Set<PosixFileModeBit>) {
        if (noFollowLinks) {
            throw UnsupportedOperationException("Cannot set mode for symbolic links")
        }
        val modeInt = mode.toInt()
        try {
            Syscall.chmod(path, modeInt)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    @Throws(IOException::class)
    override fun setSeLinuxContext(context: ByteString) {
        try {
            if (noFollowLinks) {
                Syscall.lsetfilecon(path, context)
            } else {
                Syscall.setfilecon(path, context)
            }
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    @Throws(IOException::class)
    override fun restoreSeLinuxContext() {
        val path = if (noFollowLinks) {
            path
        } else {
            try {
                Syscall.realpath(path)
            } catch (e: SyscallException) {
                throw e.toFileSystemException(path.toString())
            }
        }
        try {
            Syscall.selinux_android_restorecon(path, 0)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    companion object {
        private val NAME = LinuxFileSystemProvider.scheme

        val SUPPORTED_NAMES = setOf("basic", "posix", NAME)
    }
}
