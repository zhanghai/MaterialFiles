/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Path;

public class PathListDirectoryStream implements DirectoryStream<Path> {

    @NonNull
    private final List<Path> mPaths;

    @NonNull
    private final Filter<? super Path> mFilter;

    @Nullable
    private PathIterator mIterator;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public PathListDirectoryStream(@NonNull List<Path> paths,
                                   @NonNull Filter<? super Path> filter) {
        mPaths = paths;
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
            mIterator = new PathIterator();
            return mIterator;
        }
    }

    @Override
    public void close() {
        synchronized (mLock) {
            mClosed = true;
        }
    }

    private class PathIterator implements Iterator<Path> {

        @NonNull
        private final Iterator<Path> mIterator;

        @Nullable
        private Path mNextPath;

        private boolean mEndOfStream;

        public PathIterator() {
            mIterator = mPaths.iterator();
        }

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
                if (mClosed) {
                    return null;
                }
                if (!mIterator.hasNext()) {
                    return null;
                }
                Path path = mIterator.next();
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
