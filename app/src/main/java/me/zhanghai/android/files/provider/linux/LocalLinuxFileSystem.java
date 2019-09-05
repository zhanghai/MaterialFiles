/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.Path;
import java8.nio.file.PathMatcher;
import java8.nio.file.WatchService;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;

class LocalLinuxFileSystem extends FileSystem implements ByteStringListPathFactory {

    static final byte SEPARATOR = '/';

    private static final ByteString SEPARATOR_BYTE_STRING = ByteString.ofByte(SEPARATOR);
    private static final String SEPARATOR_STRING = Character.toString((char) SEPARATOR);

    @NonNull
    private final LinuxFileSystem mFileSystem;

    @NonNull
    private final LinuxFileSystemProvider mProvider;

    @NonNull
    private final LinuxPath mRootDirectory;

    @NonNull
    private final LinuxPath mDefaultDirectory;

    public LocalLinuxFileSystem(@NonNull LinuxFileSystem fileSystem,
                                @NonNull LinuxFileSystemProvider provider) {
        mFileSystem = fileSystem;
        mProvider = provider;

        mRootDirectory = new LinuxPath(mFileSystem, SEPARATOR_BYTE_STRING);
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }

        String userDir = System.getenv("user.dir");
        if (userDir == null) {
            userDir = SEPARATOR_STRING;
        }
        mDefaultDirectory = new LinuxPath(mFileSystem, ByteString.fromString(userDir));
        if (!mDefaultDirectory.isAbsolute()) {
            throw new AssertionError("Default directory must be absolute");
        }
    }

    @NonNull
    LinuxPath getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    LinuxPath getDefaultDirectory() {
        return mDefaultDirectory;
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @NonNull
    @Override
    public String getSeparator() {
        return SEPARATOR_STRING;
    }

    @NonNull
    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singletonList(mRootDirectory);
    }

    @NonNull
    @Override
    public Iterable<FileStore> getFileStores() {
        //noinspection unchecked
        return (Iterable<FileStore>) (Iterator<?>) LocalLinuxFileStore.getFileStores(this);
    }

    @NonNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        return LinuxFileAttributeView.SUPPORTED_NAMES;
    }

    @NonNull
    @Override
    public LinuxPath getPath(@NonNull String first, @NonNull String... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        ByteStringBuilder pathBuilder = new ByteStringBuilder(ByteString.fromString(first));
        for (String name : more) {
            Objects.requireNonNull(name);
            pathBuilder
                    .append(SEPARATOR)
                    .append(ByteString.fromString(name));
        }
        ByteString path = pathBuilder.toByteString();
        return new LinuxPath(mFileSystem, path);
    }

    @NonNull
    @Override
    public LinuxPath getPath(@NonNull ByteString first, @NonNull ByteString... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        ByteStringBuilder pathBuilder = new ByteStringBuilder(first);
        for (ByteString name : more) {
            Objects.requireNonNull(name);
            pathBuilder
                    .append(SEPARATOR)
                    .append(name);
        }
        ByteString path = pathBuilder.toByteString();
        return new LinuxPath(mFileSystem, path);
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher(@NonNull String syntaxAndPattern) {
        Objects.requireNonNull(syntaxAndPattern);
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public LinuxUserPrincipalLookupService getUserPrincipalLookupService() {
        return LinuxUserPrincipalLookupService.getInstance();
    }

    @NonNull
    @Override
    public WatchService newWatchService() throws IOException {
        return new LocalLinuxWatchService();
    }
}
