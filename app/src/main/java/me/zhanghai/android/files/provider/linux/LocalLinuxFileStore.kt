/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.os.Parcel
import android.os.Parcelable
import android.system.OsConstants
import android.system.StructStatVfs
import java8.nio.file.attribute.FileAttributeView
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.FileStoreNotFoundException
import me.zhanghai.android.files.provider.common.PosixFileStore
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.linux.syscall.Constants
import me.zhanghai.android.files.provider.linux.syscall.Int32Ref
import me.zhanghai.android.files.provider.linux.syscall.StructMntent
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.provider.linux.syscall.Syscalls
import me.zhanghai.android.files.util.andInv
import me.zhanghai.android.files.util.hasBits
import me.zhanghai.android.files.util.readParcelable
import java.io.IOException

internal class LocalLinuxFileStore : PosixFileStore, Parcelable {
    private val path: LinuxPath
    private lateinit var mntent: StructMntent

    @Throws(IOException::class)
    constructor(path: LinuxPath) {
        this.path = path
        refresh()
    }

    private constructor(fileSystem: LocalLinuxFileSystem, mntent: StructMntent) {
        path = fileSystem.getPath(mntent.mnt_dir)
        this.mntent = mntent
    }

    @Throws(IOException::class)
    override fun refresh() {
        this.mntent = try {
            findMountEntry(path)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        } ?: throw FileStoreNotFoundException(path.toString())
    }

    @Throws(SyscallException::class)
    private fun findMountEntry(path: LinuxPath): StructMntent? {
        val entries = mutableMapOf<LinuxPath, StructMntent>()
        // The last mount entry for the same path will win because we are putting them into a Map,
        // so no need to traverse in reverse order like other implementations.
        for (mntent in getMountEntries()) {
            val entryPath = path.fileSystem.getPath(mntent.mnt_dir)
            entries[entryPath] = mntent
        }
        var path = path
        while (true) {
            val mntent = entries[path]
            if (mntent != null) {
                return mntent
            }
            path = path.parent ?: break
        }
        return null
    }

    override fun name(): String = mntent.mnt_dir.toString()

    override fun type(): String = mntent.mnt_type.toString()

    override fun isReadOnly(): Boolean = Syscalls.hasmntopt(mntent, OPTION_RO)

    @Throws(IOException::class)
    override fun setReadOnly(readOnly: Boolean) {
        // Fetch the latest mount entry before we remount.
        refresh()
        if (isReadOnly == readOnly) {
            return
        }
        var (flags, options) = getFlagsFromOptions(mntent.mnt_opts)
        flags = if (readOnly) {
            flags or Constants.MS_RDONLY
        } else {
            flags andInv Constants.MS_RDONLY
        }
        val data = options.cstr
        try {
            remount(mntent.mnt_fsname, mntent.mnt_dir, mntent.mnt_type, flags, data)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(mntent.mnt_dir.toString())
        }
        refresh()
    }

    private fun getFlagsFromOptions(options: ByteString): Pair<Long, ByteString> {
        var flags = 0L
        val builder = ByteStringBuilder()
        for (option in options.split(OPTIONS_DELIMITER)) {
            val flag = OPTION_FLAG_MAP[option]
            if (flag != null) {
                flags = flags or flag
            } else {
                if (!builder.isEmpty) {
                    builder.append(OPTIONS_DELIMITER)
                }
                builder.append(option)
            }
        }
        return flags to builder.toByteString()
    }

    @Throws(SyscallException::class)
    private fun remount(
        source: ByteString?,
        target: ByteString,
        fileSystemType: ByteString?,
        mountFlags: Long,
        data: ByteArray?
    ) {
        val mountFlags = mountFlags or Constants.MS_REMOUNT
        try {
            Syscalls.mount(source, target, fileSystemType, mountFlags, data)
        } catch (e: SyscallException) {
            val readOnly = mountFlags.hasBits(Constants.MS_RDONLY)
            val isReadOnlyError = e.errno == OsConstants.EACCES || e.errno == OsConstants.EROFS
            if (readOnly || !isReadOnlyError) {
                throw e
            }
            try {
                val fd = Syscalls.open(source!!, OsConstants.O_RDONLY, 0)
                try {
                    Syscalls.ioctl_int(fd, Constants.BLKROSET, Int32Ref(0))
                } finally {
                    Syscalls.close(fd)
                }
                Syscalls.mount(source, target, fileSystemType, mountFlags, data)
            } catch (e2: SyscallException) {
                e.addSuppressed(e2)
                throw e
            }
        }
    }

    @Throws(IOException::class)
    override fun getTotalSpace(): Long {
        val statVfs = getStatVfs()
        return statVfs.f_blocks * statVfs.f_bsize
    }

    @Throws(IOException::class)
    override fun getUsableSpace(): Long {
        val statVfs = getStatVfs()
        return statVfs.f_bavail * statVfs.f_bsize
    }

    @Throws(IOException::class)
    override fun getUnallocatedSpace(): Long {
        val statVfs = getStatVfs()
        return statVfs.f_bfree * statVfs.f_bsize
    }

    @Throws(IOException::class)
    private fun getStatVfs(): StructStatVfs =
        try {
            Syscalls.statvfs(path.toByteString())
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path.toString())
        }

    override fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        LinuxFileSystemProvider.supportsFileAttributeView(type)

    override fun supportsFileAttributeView(name: String): Boolean =
        name in LinuxFileAttributeView.SUPPORTED_NAMES

    private constructor(source: Parcel) {
        path = source.readParcelable()!!
        mntent = source.readParcelable()!!
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, flags)
        dest.writeParcelable(mntent, flags)
    }

    companion object {
        private val PATH_PROC_SELF_MOUNTS = "/proc/self/mounts".toByteString()

        private val MODE_R = "r".toByteString()

        private val OPTIONS_DELIMITER = ",".toByteString()
        private val OPTION_RO = "ro".toByteString()
        // @see https://android.googlesource.com/platform/system/core/+/master/fs_mgr/fs_mgr_fstab.cpp
        //      kMountFlagsList
        // @see https://github.com/mmalecki/util-linux/blob/master/mount-deprecated/mount.c opt_map
        // @see https://android.googlesource.com/platform/external/toybox/+/refs/heads/master/toys/lsb/mount.c
        //      flag_opts()
        // @see http://lists.landley.net/pipermail/toybox-landley.net/2012-August/000628.html
        private val OPTION_FLAG_MAP = mapOf(
            "defaults" to 0L,
            "ro" to Constants.MS_RDONLY,
            "rw" to 0L,
            "nosuid" to Constants.MS_NOSUID,
            "suid" to 0L,
            "nodev" to Constants.MS_NODEV,
            "dev" to 0L,
            "noexec" to Constants.MS_NOEXEC,
            "exec" to 0L,
            "sync" to Constants.MS_SYNCHRONOUS,
            "async" to 0L,
            "remount" to Constants.MS_REMOUNT,
            "mand" to Constants.MS_MANDLOCK,
            "nomand" to 0L,
            "dirsync" to Constants.MS_DIRSYNC,
            "noatime" to Constants.MS_NOATIME,
            "atime" to 0L,
            "nodiratime" to Constants.MS_NODIRATIME,
            "diratime" to 0L,
            "bind" to Constants.MS_BIND,
            "rbind" to (Constants.MS_BIND or Constants.MS_REC),
            "move" to Constants.MS_MOVE,
            "rec" to Constants.MS_REC,
            "verbose" to Constants.MS_VERBOSE,
            "silent" to Constants.MS_SILENT,
            "loud" to 0L,
            //"posixacl" to Constants.MS_POSIXACL,
            //"noposixacl" to 0L,
            "unbindable" to Constants.MS_UNBINDABLE,
            "runbindable" to (Constants.MS_UNBINDABLE or Constants.MS_REC),
            "private" to Constants.MS_PRIVATE,
            "rprivate" to (Constants.MS_PRIVATE or Constants.MS_REC),
            "slave" to Constants.MS_SLAVE,
            "rslave" to (Constants.MS_SLAVE or Constants.MS_REC),
            "shared" to Constants.MS_SHARED,
            "rshared" to (Constants.MS_SHARED or Constants.MS_REC),
            "relatime" to Constants.MS_RELATIME,
            "norelatime" to 0L,
            //"kernmount" to Constants.MS_KERNMOUNT,
            "iversion" to Constants.MS_I_VERSION,
            "noiversion" to 0L,
            "strictatime" to Constants.MS_STRICTATIME,
            "nostrictatime" to 0L,
            "lazytime" to Constants.MS_LAZYTIME,
            "nolazytime" to 0L,
            //"submount" to Constants.MS_SUBMOUNT,
            //"noremotelock" to Constants.MS_NOREMOTELOCK,
            //"remotelock" to 0L,
            //"nosec" to Constants.MS_NOSEC,
            //"sec" to 0L,
            //"born" to Constants.MS_BORN,
            //"active" to Constants.MS_ACTIVE,
            "nouser" to Constants.MS_NOUSER,
            "user" to 0L
        ).mapKeys { it.key.toByteString() }

        fun getFileStores(fileSystem: LocalLinuxFileSystem): List<LocalLinuxFileStore> {
            val entries = try {
                getMountEntries()
            } catch (e: SyscallException) {
                e.printStackTrace()
                return emptyList()
            }
            return entries.map { LocalLinuxFileStore(fileSystem, it) }
        }

        @Throws(SyscallException::class)
        private fun getMountEntries(): List<StructMntent> {
            val entries = mutableListOf<StructMntent>()
            val file = Syscalls.setmntent(PATH_PROC_SELF_MOUNTS, MODE_R)
            try {
                while (true) {
                    val mntent = Syscalls.getmntent(file) ?: break
                    entries += mntent
                }
            } finally {
                Syscalls.endmntent(file)
            }
            return entries
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalLinuxFileStore> {
            override fun createFromParcel(source: Parcel): LocalLinuxFileStore =
                LocalLinuxFileStore(source)

            override fun newArray(size: Int): Array<LocalLinuxFileStore?> = arrayOfNulls(size)
        }
    }
}
