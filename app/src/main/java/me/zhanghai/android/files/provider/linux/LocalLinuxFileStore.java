/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import android.system.StructStatVfs;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.FileStoreNotFoundException;
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.Int32Ref;
import me.zhanghai.android.files.provider.linux.syscall.StructMntent;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.util.MapBuilder;
import me.zhanghai.java.functional.Functional;

class LocalLinuxFileStore extends PosixFileStore implements Parcelable {

    private static final ByteString PATH_PROC_SELF_MOUNTS = ByteString.fromString(
            "/proc/self/mounts");
    private static final ByteString MODE_R = ByteString.fromString("r");
    private static final ByteString OPTIONS_DELIMITER = ByteString.ofByte((byte) ',');
    private static final ByteString OPTION_RO = ByteString.fromString("ro");
    // @see https://android.googlesource.com/platform/system/core/+/master/fs_mgr/fs_mgr_fstab.cpp
    //      kMountFlagsList
    // @see https://github.com/mmalecki/util-linux/blob/master/mount-deprecated/mount.c opt_map
    // @see https://android.googlesource.com/platform/external/toybox/+/refs/heads/master/toys/lsb/mount.c
    //      flag_opts()
    // @see http://lists.landley.net/pipermail/toybox-landley.net/2012-August/000628.html
    private static final Map<ByteString, Long> OPTION_FLAG_MAP =
            MapBuilder.<ByteString, Long>newHashMap()
                    .put(ByteString.fromString("defaults"), 0L)
                    .put(ByteString.fromString("ro"), Constants.MS_RDONLY)
                    .put(ByteString.fromString("rw"), 0L)
                    .put(ByteString.fromString("nosuid"), Constants.MS_NOSUID)
                    .put(ByteString.fromString("suid"), 0L)
                    .put(ByteString.fromString("nodev"), Constants.MS_NODEV)
                    .put(ByteString.fromString("dev"), 0L)
                    .put(ByteString.fromString("noexec"), Constants.MS_NOEXEC)
                    .put(ByteString.fromString("exec"), 0L)
                    .put(ByteString.fromString("sync"), Constants.MS_SYNCHRONOUS)
                    .put(ByteString.fromString("async"), 0L)
                    .put(ByteString.fromString("remount"), Constants.MS_REMOUNT)
                    .put(ByteString.fromString("mand"), Constants.MS_MANDLOCK)
                    .put(ByteString.fromString("nomand"), 0L)
                    .put(ByteString.fromString("dirsync"), Constants.MS_DIRSYNC)
                    .put(ByteString.fromString("noatime"), Constants.MS_NOATIME)
                    .put(ByteString.fromString("atime"), 0L)
                    .put(ByteString.fromString("nodiratime"), Constants.MS_NODIRATIME)
                    .put(ByteString.fromString("diratime"), 0L)
                    .put(ByteString.fromString("bind"), Constants.MS_BIND)
                    .put(ByteString.fromString("rbind"), Constants.MS_BIND | Constants.MS_REC)
                    .put(ByteString.fromString("move"), Constants.MS_MOVE)
                    .put(ByteString.fromString("rec"), Constants.MS_REC)
                    .put(ByteString.fromString("verbose"), Constants.MS_VERBOSE)
                    .put(ByteString.fromString("silent"), Constants.MS_SILENT)
                    .put(ByteString.fromString("loud"), 0L)
                    //.put(ByteString.fromString("posixacl"), Constants.MS_POSIXACL)
                    //.put(ByteString.fromString("noposixacl"), 0L)
                    .put(ByteString.fromString("unbindable"), Constants.MS_UNBINDABLE)
                    .put(ByteString.fromString("runbindable"), Constants.MS_UNBINDABLE
                            | Constants.MS_REC)
                    .put(ByteString.fromString("private"), Constants.MS_PRIVATE)
                    .put(ByteString.fromString("rprivate"), Constants.MS_PRIVATE | Constants.MS_REC)
                    .put(ByteString.fromString("slave"), Constants.MS_SLAVE)
                    .put(ByteString.fromString("rslave"), Constants.MS_SLAVE | Constants.MS_REC)
                    .put(ByteString.fromString("shared"), Constants.MS_SHARED)
                    .put(ByteString.fromString("rshared"), Constants.MS_SHARED | Constants.MS_REC)
                    .put(ByteString.fromString("relatime"), Constants.MS_RELATIME)
                    .put(ByteString.fromString("norelatime"), 0L)
                    //.put(ByteString.fromString("kernmount"), Constants.MS_KERNMOUNT)
                    .put(ByteString.fromString("iversion"), Constants.MS_I_VERSION)
                    .put(ByteString.fromString("noiversion"), 0L)
                    .put(ByteString.fromString("strictatime"), Constants.MS_STRICTATIME)
                    .put(ByteString.fromString("nostrictatime"), 0L)
                    .put(ByteString.fromString("lazytime"), Constants.MS_LAZYTIME)
                    .put(ByteString.fromString("nolazytime"), 0L)
                    //.put(ByteString.fromString("submount"), Constants.MS_SUBMOUNT)
                    //.put(ByteString.fromString("noremotelock"), Constants.MS_NOREMOTELOCK)
                    //.put(ByteString.fromString("remotelock"), 0L)
                    //.put(ByteString.fromString("nosec"), Constants.MS_NOSEC)
                    //.put(ByteString.fromString("sec"), 0L)
                    //.put(ByteString.fromString("born"), Constants.MS_BORN)
                    //.put(ByteString.fromString("active"), Constants.MS_ACTIVE)
                    .put(ByteString.fromString("nouser"), Constants.MS_NOUSER)
                    .put(ByteString.fromString("user"), 0L)
                    .build();

    @NonNull
    private final LinuxPath mPath;
    @NonNull
    private StructMntent mMntent;

    public LocalLinuxFileStore(@NonNull LinuxPath path) throws IOException {
        mPath = path;
        refresh();
    }

    private LocalLinuxFileStore(@NonNull LocalLinuxFileSystem fileSystem,
                                @NonNull StructMntent mntent) {
        mPath = fileSystem.getPath(mntent.mnt_dir);
        mMntent = mntent;
    }

    @NonNull
    public static List<LocalLinuxFileStore> getFileStores(
            @NonNull LocalLinuxFileSystem fileSystem) {
        List<StructMntent> entries;
        try {
            entries = getMountEntries();
        } catch (SyscallException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return Functional.map(entries, mntent -> new LocalLinuxFileStore(fileSystem, mntent));
    }

    @Override
    public void refresh() throws IOException {
        StructMntent mntent;
        try {
            mntent = findMountEntry(mPath);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        if (mntent == null) {
            throw new FileStoreNotFoundException(mPath.toString());
        }
        mMntent = mntent;
    }

    @Nullable
    private static StructMntent findMountEntry(@NonNull LinuxPath path) throws SyscallException {
        Map<LinuxPath, StructMntent> entries = new HashMap<>();
        // The last mount entry for the same path will win because we are putting them into a Map,
        // so no need to traverse in reverse order like other implementations.
        for (StructMntent mntent : getMountEntries()) {
            LinuxPath entryPath = path.getFileSystem().getPath(mntent.mnt_dir);
            entries.put(entryPath, mntent);
        }
        do {
            StructMntent mntent = entries.get(path);
            if (mntent != null) {
                return mntent;
            }
        } while ((path = path.getParent()) != null);
        return null;
    }

    @NonNull
    private static List<StructMntent> getMountEntries() throws SyscallException {
        List<StructMntent> entries = new ArrayList<>();
        long file = Syscalls.setmntent(PATH_PROC_SELF_MOUNTS, MODE_R);
        try {
            StructMntent mntent;
            while ((mntent = Syscalls.getmntent(file)) != null) {
                entries.add(mntent);
            }
        } finally {
            Syscalls.endmntent(file);
        }
        return entries;
    }

    @NonNull
    @Override
    public String name() {
        return mMntent.mnt_dir.toString();
    }

    @NonNull
    @Override
    public String type() {
        return mMntent.mnt_type.toString();
    }

    @Override
    public boolean isReadOnly() {
        return Syscalls.hasmntopt(mMntent, OPTION_RO);
    }

    public void setReadOnly(boolean readOnly) throws IOException {
        // Fetch the latest mount entry before we remount.
        refresh();
        if (isReadOnly() == readOnly) {
            return;
        }
        Pair<Long, ByteString> flagsAndOptions = getFlagsFromOptions(mMntent.mnt_opts);
        long flags = flagsAndOptions.first;
        if (readOnly) {
            flags |= Constants.MS_RDONLY;
        } else {
            flags &= ~Constants.MS_RDONLY;
        }
        ByteString options = flagsAndOptions.second;
        byte[] data = options.toNullTerminatedString();
        try {
            remount(mMntent.mnt_fsname, mMntent.mnt_dir, mMntent.mnt_type, flags, data);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mMntent.mnt_dir.toString());
        }
        refresh();
    }

    @NonNull
    private static Pair<Long, ByteString> getFlagsFromOptions(@NonNull ByteString options) {
        long flags = 0;
        ByteStringBuilder builder = new ByteStringBuilder();
        for (ByteString option : options.split(OPTIONS_DELIMITER)) {
            if (OPTION_FLAG_MAP.containsKey(option)) {
                flags |= OPTION_FLAG_MAP.get(option);
            } else {
                if (!builder.isEmpty()) {
                    builder.append(OPTIONS_DELIMITER);
                }
                builder.append(option);
            }
        }
        return new Pair<>(flags, builder.toByteString());
    }

    private static void remount(@Nullable ByteString source, @NonNull ByteString target,
                                @Nullable ByteString fileSystemType, long mountFlags,
                                @Nullable byte[] data) throws SyscallException {
        mountFlags |= Constants.MS_REMOUNT;
        try {
            Syscalls.mount(source, target, fileSystemType, mountFlags, data);
        } catch (SyscallException e) {
            boolean readOnly = (mountFlags & Constants.MS_RDONLY) == Constants.MS_RDONLY;
            boolean isReadOnlyError = e.getErrno() == OsConstants.EACCES
                    || e.getErrno() == OsConstants.EROFS;
            if (readOnly || !isReadOnlyError) {
                throw e;
            }
            try {
                FileDescriptor fd = Syscalls.open(source, OsConstants.O_RDONLY, 0);
                try {
                    Syscalls.ioctl_int(fd, Constants.BLKROSET, new Int32Ref(0));
                } finally {
                    Syscalls.close(fd);
                }
                Syscalls.mount(source, target, fileSystemType, mountFlags, data);
            } catch (SyscallException e2) {
                e.addSuppressed(e2);
                throw e;
            }
        }
    }

    @Override
    public long getTotalSpace() throws IOException {
        StructStatVfs statVfs = getStatVfs();
        return statVfs.f_blocks * statVfs.f_bsize;
    }

    @Override
    public long getUsableSpace() throws IOException {
        StructStatVfs statVfs = getStatVfs();
        return statVfs.f_bavail * statVfs.f_bsize;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        StructStatVfs statVfs = getStatVfs();
        return statVfs.f_bfree * statVfs.f_bsize;
    }

    @NonNull
    private StructStatVfs getStatVfs() throws IOException {
        try {
            return Syscalls.statvfs(mPath.toByteString());
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return LinuxFileSystemProvider.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        Objects.requireNonNull(name);
        return LinuxFileAttributeView.SUPPORTED_NAMES.contains(name);
    }


    public static final Creator<LocalLinuxFileStore> CREATOR = new Creator<LocalLinuxFileStore>() {
        @Override
        public LocalLinuxFileStore createFromParcel(Parcel source) {
            return new LocalLinuxFileStore(source);
        }
        @Override
        public LocalLinuxFileStore[] newArray(int size) {
            return new LocalLinuxFileStore[size];
        }
    };

    protected LocalLinuxFileStore(Parcel in) {
        mPath = in.readParcelable(LinuxPath.class.getClassLoader());
        mMntent = in.readParcelable(StructMntent.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPath, flags);
        dest.writeParcelable(mMntent, flags);
    }
}
