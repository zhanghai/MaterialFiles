/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;

public abstract class AbstractPath<PathType extends AbstractPath<PathType>>
        extends CovariantPath<PathType> {

    @Nullable
    @Override
    public PathType getFileName() {
        int nameCount = getNameCount();
        if (nameCount == 0) {
            return null;
        }
        return getName(nameCount - 1);
    }

    @Nullable
    @Override
    public PathType getParent() {
        int nameCount = getNameCount();
        switch (nameCount) {
            case 0:
                return null;
            case 1:
                return getRoot();
            default:
                return getRoot().resolve(subpath(0, nameCount - 1));
        }
    }

    @Override
    public boolean startsWith(@NonNull String other) {
        Objects.requireNonNull(other);
        return startsWith(getFileSystem().getPath(other));
    }

    @Override
    public boolean endsWith(@NonNull String other) {
        Objects.requireNonNull(other);
        return endsWith(getFileSystem().getPath(other));
    }

    @NonNull
    @Override
    public PathType resolve(@NonNull String other) {
        Objects.requireNonNull(other);
        return resolve(getFileSystem().getPath(other));
    }

    @NonNull
    @Override
    public PathType resolveSibling(@NonNull Path other) {
        Objects.requireNonNull(other);
        PathType parent = getParent();
        //noinspection unchecked
        return parent != null ? parent.resolve(other) : (PathType) other;
    }

    @NonNull
    @Override
    public PathType resolveSibling(@NonNull String other) {
        Objects.requireNonNull(other);
        return resolveSibling(getFileSystem().getPath(other));
    }

    @NonNull
    @Override
    public Iterator<Path> iterator() {
        return new NameIterator();
    }

    @NonNull
    @Override
    public final WatchKey register(@NonNull WatchService watcher,
                                   @NonNull WatchEvent.Kind<?>... events) throws IOException {
        Objects.requireNonNull(watcher);
        Objects.requireNonNull(events);
        return register(watcher, events, new WatchEvent.Modifier[0]);
    }

    private class NameIterator implements Iterator<Path> {

        private int mNameIndex = 0;

        @Override
        public boolean hasNext() {
            return mNameIndex < getNameCount();
        }

        @NonNull
        @Override
        public Path next() {
            if (mNameIndex >= getNameCount()) {
                throw new NoSuchElementException();
            }
            Path name = getName(mNameIndex);
            ++mNameIndex;
            return name;
        }
    }
}
