/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.StructStatVfs;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.FileStore;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LocalLinuxFileStore extends FileStore {

    @NonNull
    private final ByteString mPath;

    public LocalLinuxFileStore(@NonNull ByteString path) {
        mPath = path;
    }

    @NonNull
    @Override
    public String name() {
        // TODO: Use getmntent_r.
        return null;
    }

    @NonNull
    @Override
    public String type() {
        // TODO: Use getmntent_r.
        return null;
    }

    @Override
    public boolean isReadOnly() {
        // TODO: Use getmntent_r.
        return false;
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
            return Syscalls.statvfs(mPath);
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
