/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.util.Pair;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.ClosedFileSystemException;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.NotDirectoryException;
import java8.nio.file.Path;
import java8.nio.file.PathMatcher;
import java8.nio.file.WatchService;
import java8.nio.file.attribute.UserPrincipalLookupService;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.archive.reader.ArchiveReader;
import me.zhanghai.android.files.util.SetBuilder;

class ArchiveFileSystem extends FileSystem {

    static final char SEPARATOR = '/';

    private static final String SEPARATOR_STRING = Character.toString(SEPARATOR);

    @NonNull
    private final ArchivePath mRootDirectory = new ArchivePath(this, "/");
    {
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }
    }

    @NonNull
    private final ArchiveFileSystemProvider mProvider;

    @NonNull
    private final Path mArchiveFile;

    @NonNull
    private final Object mLock = new Object();

    private boolean mOpen;

    private Map<Path, ArchiveEntry> mEntries;

    private Map<Path, List<Path>> mTree;

    public ArchiveFileSystem(@NonNull ArchiveFileSystemProvider provider, @NonNull Path archiveFile)
            throws IOException {
        mProvider = provider;
        mArchiveFile = archiveFile;

        open();
    }

    @NonNull
    Path getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Path getDefaultDirectory() {
        return mRootDirectory;
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
    }

    @NonNull
    Path getArchiveFile() {
        return mArchiveFile;
    }

    @NonNull
    ArchiveEntry getEntry(@NonNull Path path) throws IOException {
        synchronized (mLock) {
            ensureOpenLocked();
            return getEntryLocked(path);
        }
    }

    @NonNull
    private ArchiveEntry getEntryLocked(@NonNull Path path) throws IOException {
        synchronized (mLock) {
            ArchiveEntry entry = mEntries.get(path);
            if (entry == null) {
                throw new NoSuchFileException(path.toString());
            }
            return entry;
        }
    }

    @NonNull
    InputStream newInputStream(@NonNull Path file) throws IOException {
        synchronized (mLock) {
            ensureOpenLocked();
            ArchiveEntry entry = getEntryLocked(file);
            return ArchiveReader.newInputStream(mArchiveFile, entry);
        }
    }

    @NonNull
    List<Path> getChildren(@NonNull Path directory) throws IOException {
        synchronized (mLock) {
            ensureOpenLocked();
            ArchiveEntry entry = getEntryLocked(directory);
            if (!entry.isDirectory()) {
                throw new NotDirectoryException(directory.toString());
            }
            return mTree.get(directory);
        }
    }

    @NonNull
    String readSymbolicLink(@NonNull Path link) throws IOException {
        synchronized (mLock) {
            ensureOpenLocked();
            ArchiveEntry entry = getEntryLocked(link);
            return ArchiveReader.readSymbolicLink(link, entry);
        }
    }

    private void ensureOpenLocked() {
        if (!mOpen) {
            throw new ClosedFileSystemException();
        }
    }

    private void open() throws IOException {
        synchronized (mLock) {
            if (mOpen) {
                throw new IllegalStateException();
            }
            Pair<Map<Path, ArchiveEntry>, Map<Path, List<Path>>> entriesAndTree =
                    ArchiveReader.readEntries(mArchiveFile, mRootDirectory);
            mEntries = entriesAndTree.first;
            mTree = entriesAndTree.second;
            mOpen = true;
        }
    }

    @Override
    public void close() {
        synchronized (mLock) {
            if (!mOpen) {
                return;
            }
            mProvider.removeFileSystem(this);
            mOpen = false;
            mEntries = null;
            mTree = null;
        }
    }

    @Override
    public boolean isOpen() {
        synchronized (mLock) {
            return mOpen;
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
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
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        return ArchiveFileAttributeView.SUPPORTED_NAMES;
    }

    @NonNull
    @Override
    public Path getPath(@NonNull String first, @NonNull String... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        StringBuilder pathBuilder = new StringBuilder(first);
        for (String name : more) {
            pathBuilder
                    .append(SEPARATOR)
                    .append(name);
        }
        String path = pathBuilder.toString();
        return new ArchivePath(this, path);
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        Objects.requireNonNull(syntaxAndPattern);
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ArchiveFileSystem that = (ArchiveFileSystem) object;
        return Objects.equals(mArchiveFile, that.mArchiveFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mArchiveFile);
    }
}
