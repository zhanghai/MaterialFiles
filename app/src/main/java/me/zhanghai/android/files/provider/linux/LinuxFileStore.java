/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.StructStatVfs;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileStore;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.attribute.FileStoreAttributeView;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.provider.remote.RemotableFileStore;
import me.zhanghai.android.files.provider.remote.RemoteFileStore;

public class LinuxFileStore extends FileStore implements RemotableFileStore {

    @NonNull
    private final String mPath;

    LinuxFileStore(@NonNull String path) {
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
            throw e.toFileSystemException(mPath);
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

    @Nullable
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(@NonNull Class<V> type) {
        Objects.requireNonNull(type);
        return null;
    }

    @Nullable
    @Override
    public Object getAttribute(@NonNull String attribute) {
        Objects.requireNonNull(attribute);
        throw new UnsupportedOperationException();
    }


    @NonNull
    @Override
    public RemoteFileStore toRemote() {
        return new RemoteLinuxFileStore(this);
    }
}
