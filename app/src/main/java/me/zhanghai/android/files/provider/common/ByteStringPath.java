/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;

public class ByteStringPath implements Parcelable, Path {

    @NonNull
    private final ByteString mByteString;

    public ByteStringPath(@NonNull ByteString byteString) {
        mByteString = byteString;
    }

    @NonNull
    public ByteString toByteString() {
        return mByteString;
    }

    /**
     * @deprecated Use {@link #toByteString()} instead.
     */
    @NonNull
    @Override
    public String toString() {
        return mByteString.toString();
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


    public static final Creator<ByteStringPath> CREATOR = new Creator<ByteStringPath>() {
        @Override
        public ByteStringPath createFromParcel(Parcel source) {
            return new ByteStringPath(source);
        }
        @Override
        public ByteStringPath[] newArray(int size) {
            return new ByteStringPath[size];
        }
    };

    protected ByteStringPath(Parcel in) {
        mByteString = in.readParcelable(ByteString.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mByteString, flags);
    }
}
