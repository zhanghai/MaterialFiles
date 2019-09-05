/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.StructStatVfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.common.AbstractFileStore;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.FileStoreNotFoundException;
import me.zhanghai.android.files.provider.linux.syscall.StructMntent;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LocalLinuxFileStore extends AbstractFileStore {

    private static final ByteString PATH_PROC_SELF_MOUNTS = ByteString.fromString(
            "/proc/self/mounts");
    private static final ByteString MODE_R = ByteString.fromString("r");
    private static final ByteString OPTION_RO = ByteString.fromString("ro");

    @NonNull
    private final LinuxPath mPath;
    @NonNull
    private final StructMntent mMntent;
    private final boolean mReadOnly;

    public LocalLinuxFileStore(@NonNull LocalLinuxFileSystem fileSystem,
                               @NonNull StructMntent mntent) {
        mPath = fileSystem.getPath(mntent.mnt_dir);
        mMntent = mntent;
        mReadOnly = Syscalls.hasmntopt(mMntent, OPTION_RO);
    }

    public LocalLinuxFileStore(@NonNull LinuxPath path) throws IOException {
        mPath = path;
        StructMntent mntent;
        try {
            mntent = findMountEntry(path);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path.toString());
        }
        if (mntent == null) {
            throw new FileStoreNotFoundException(path.toString());
        }
        mMntent = mntent;
        mReadOnly = Syscalls.hasmntopt(mMntent, OPTION_RO);
    }

    @Nullable
    private static StructMntent findMountEntry(@NonNull LinuxPath path) throws SyscallException {
        Map<LinuxPath, StructMntent> entries = new HashMap<>();
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
    static List<StructMntent> getMountEntries() throws SyscallException {
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
        return mMntent.mnt_fsname.toString();
    }

    @NonNull
    @Override
    public String type() {
        return mMntent.mnt_type.toString();
    }

    @Override
    public boolean isReadOnly() {
        return mReadOnly;
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
        throw new AssertionError();
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        throw new AssertionError();
    }

    static boolean supportsFileAttributeView_(@NonNull Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return LinuxFileSystemProvider.supportsFileAttributeView(type);
    }

    static boolean supportsFileAttributeView_(@NonNull String name) {
        Objects.requireNonNull(name);
        return LinuxFileAttributeView.SUPPORTED_NAMES.contains(name);
    }
}
