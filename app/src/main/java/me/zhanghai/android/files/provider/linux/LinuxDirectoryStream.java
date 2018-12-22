/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.linux.syscall.StructDirent;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LinuxDirectoryStream implements DirectoryStream<Path> {

    @NonNull
    private final Path mDirectory;

    private final long mDir;

    @NonNull
    private final Filter<? super Path> mFilter;

    @Nullable
    private LinuxDirectoryIterator mIterator;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public LinuxDirectoryStream(@NonNull Path directory, long dir,
                                @NonNull Filter<? super Path> filter) {
        mDirectory = directory;
        mDir = dir;
        mFilter = filter;
    }

    @Override
    public Iterator<Path> iterator() {
        synchronized (mLock) {
            if (mClosed) {
                throw new IllegalStateException("This directory stream is closed");
            }
            if (mIterator != null) {
                throw new IllegalStateException("The iterator has already been returned");
            }
            mIterator = new LinuxDirectoryIterator();
            return mIterator;
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (mLock) {
            try {
                Syscalls.closedir(mDir);
            } catch (SyscallException e) {
                throw e.toFileSystemException(mDirectory.toString());
            }
            mClosed = true;
        }
    }

    private class LinuxDirectoryIterator implements Iterator<Path> {

        @Nullable
        private Path mNextPath;

        private boolean mEndOfStream;

        @Override
        public boolean hasNext() {
            synchronized (mLock) {
                if (mNextPath != null) {
                    return true;
                }
                if (mEndOfStream) {
                    return false;
                }
                mNextPath = getNextPathLocked();
                mEndOfStream = mNextPath == null;
                return !mEndOfStream;
            }
        }

        @Nullable
        private Path getNextPathLocked() {
            while (true) {
                StructDirent dirent;
                try {
                    dirent = Syscalls.readdir(mDir);
                } catch (SyscallException e) {
                    throw new DirectoryIteratorException(e.toFileSystemException(
                            mDirectory.toString()));
                }
                if (dirent == null) {
                    return null;
                }
                String name = dirent.d_name;
                if (Objects.equals(name, ".") || Objects.equals(name, "..")) {
                    continue;
                }
                Path path = mDirectory.resolve(dirent.d_name);
                boolean accepted;
                try {
                    accepted = mFilter.accept(path);
                } catch (IOException e) {
                    throw new DirectoryIteratorException(e);
                }
                if (!accepted) {
                    continue;
                }
                return path;
            }
        }

        @NonNull
        @Override
        public Path next() {
            synchronized (mLock) {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Path path = mNextPath;
                mNextPath = null;
                return path;
            }
        }
    }
}
