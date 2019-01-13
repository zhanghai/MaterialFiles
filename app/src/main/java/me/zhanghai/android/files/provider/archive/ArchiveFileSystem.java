/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;
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

class ArchiveFileSystem extends FileSystem implements Parcelable {

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

    private boolean mClosed;

    private boolean mReopenRequested = true;

    private Map<Path, ArchiveEntry> mEntries;

    private Map<Path, List<Path>> mTree;

    public ArchiveFileSystem(@NonNull ArchiveFileSystemProvider provider,
                             @NonNull Path archiveFile) {
        mProvider = provider;
        mArchiveFile = archiveFile;
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

    void requestReopen() {
        synchronized (mLock) {
            mReopenRequested = true;
        }
    }

    private void ensureOpenLocked() throws IOException {
        if (mReopenRequested) {
            Pair<Map<Path, ArchiveEntry>, Map<Path, List<Path>>> entriesAndTree =
                    ArchiveReader.readEntries(mArchiveFile, mRootDirectory);
            mEntries = entriesAndTree.first;
            mTree = entriesAndTree.second;
            mClosed = false;
            mReopenRequested = false;
        }
        if (mClosed) {
            throw new ClosedFileSystemException();
        }
    }

    @Override
    public void close() {
        synchronized (mLock) {
            mReopenRequested = false;
            if (mClosed) {
                return;
            }
            mProvider.removeFileSystem(this);
            mClosed = true;
            mEntries = null;
            mTree = null;
        }
    }

    @Override
    public boolean isOpen() {
        synchronized (mLock) {
            return !mClosed;
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


    public static final Creator<ArchiveFileSystem> CREATOR = new Creator<ArchiveFileSystem>() {
        @Override
        public ArchiveFileSystem createFromParcel(Parcel source) {
            Path archiveFile = source.readParcelable(Path.class.getClassLoader());
            return ArchiveFileSystemProvider.getOrNewFileSystem(archiveFile);
        }
        @Override
        public ArchiveFileSystem[] newArray(int size) {
            return new ArchiveFileSystem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mArchiveFile, flags);
    }
}
