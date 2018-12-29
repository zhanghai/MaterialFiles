/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;

public class StringPath implements Path {

    @NonNull
    private final String mString;

    public StringPath(@NonNull String string) {
        mString = string;
    }

    @NonNull
    @Override
    public String toString() {
        return mString;
    }


    @Override
    public FileSystem getFileSystem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAbsolute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getRoot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getFileName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNameCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getName(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path normalize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolve(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolve(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolveSibling(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolveSibling(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path relativize(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toRealPath(LinkOption... options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events,
                             WatchEvent.Modifier... modifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException();
    }
}
