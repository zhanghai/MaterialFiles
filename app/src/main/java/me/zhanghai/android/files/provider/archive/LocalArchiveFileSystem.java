/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
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
import me.zhanghai.android.files.provider.archive.archiver.ArchiveReader;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;

class LocalArchiveFileSystem extends FileSystem implements ByteStringListPathFactory {

    static final byte SEPARATOR = '/';

    private static final ByteString SEPARATOR_BYTE_STRING = ByteString.ofByte(SEPARATOR);
    private static final String SEPARATOR_STRING = Character.toString((char) SEPARATOR);

    @NonNull
    private final ArchiveFileSystem mFileSystem;

    @NonNull
    private final ArchiveFileSystemProvider mProvider;

    @NonNull
    private final Path mArchiveFile;

    @NonNull
    private final ArchivePath mRootDirectory;

    @NonNull
    private final Object mLock = new Object();

    private boolean mOpen = true;

    private boolean mNeedRefresh = true;

    private Map<Path, ArchiveEntry> mEntries;

    private Map<Path, List<Path>> mTree;

    LocalArchiveFileSystem(@NonNull ArchiveFileSystem fileSystem,
                           @NonNull ArchiveFileSystemProvider provider, @NonNull Path archiveFile) {
        mFileSystem = fileSystem;
        mProvider = provider;
        mArchiveFile = archiveFile;

        mRootDirectory = new ArchivePath(mFileSystem, SEPARATOR_BYTE_STRING);
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }
    }

    @NonNull
    ArchivePath getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    ArchivePath getDefaultDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Path getArchiveFile() {
        return mArchiveFile;
    }

    @NonNull
    ArchiveEntry getEntry(@NonNull Path path) throws IOException {
        synchronized (mLock) {
            ensureEntriesLocked();
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
            ensureEntriesLocked();
            ArchiveEntry entry = getEntryLocked(file);
            return ArchiveReader.newInputStream(mArchiveFile, entry);
        }
    }

    @NonNull
    List<Path> getDirectoryChildren(@NonNull Path directory) throws IOException {
        synchronized (mLock) {
            ensureEntriesLocked();
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
            ensureEntriesLocked();
            ArchiveEntry entry = getEntryLocked(link);
            return ArchiveReader.readSymbolicLink(mArchiveFile, entry);
        }
    }

    void refresh() {
        synchronized (mLock) {
            if (!mOpen) {
                throw new ClosedFileSystemException();
            }
            mNeedRefresh = true;
        }
    }

    private void ensureEntriesLocked() throws IOException {
        if (!mOpen) {
            throw new ClosedFileSystemException();
        }
        if (mNeedRefresh) {
            Pair<Map<Path, ArchiveEntry>, Map<Path, List<Path>>> entriesAndTree =
                    ArchiveReader.readEntries(mArchiveFile, mRootDirectory);
            mEntries = entriesAndTree.first;
            mTree = entriesAndTree.second;
            mNeedRefresh = false;
        }
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
    }

    @Override
    public void close() {
        synchronized (mLock) {
            if (!mOpen) {
                return;
            }
            mProvider.removeFileSystem(mFileSystem);
            mNeedRefresh = false;
            mEntries = null;
            mTree = null;
            mOpen = false;
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
    public ArchivePath getPath(@NonNull String first, @NonNull String... more) {
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
        return new ArchivePath(mFileSystem, path);
    }

    @NonNull
    @Override
    public ArchivePath getPath(@NonNull ByteString first, @NonNull ByteString... more) {
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
        return new ArchivePath(mFileSystem, path);
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
    public WatchService newWatchService() throws IOException {
        // TODO
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
        LocalArchiveFileSystem that = (LocalArchiveFileSystem) object;
        return Objects.equals(mArchiveFile, that.mArchiveFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mArchiveFile);
    }
}
