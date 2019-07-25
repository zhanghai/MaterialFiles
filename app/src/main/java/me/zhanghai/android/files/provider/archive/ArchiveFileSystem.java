/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.root.RootableFileSystem;

class ArchiveFileSystem extends RootableFileSystem implements ByteStringListPathFactory {

    static final byte SEPARATOR = LocalArchiveFileSystem.SEPARATOR;

    @NonNull
    private final Path mArchiveFile;

    ArchiveFileSystem(@NonNull ArchiveFileSystemProvider provider, @NonNull Path archiveFile) {
        super(fileSystem -> new LocalArchiveFileSystem((ArchiveFileSystem) fileSystem, provider,
                archiveFile), RootArchiveFileSystem::new);

        mArchiveFile = archiveFile;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected LocalArchiveFileSystem getLocalFileSystem() {
        return super.getLocalFileSystem();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected RootArchiveFileSystem getRootFileSystem() {
        return super.getRootFileSystem();
    }

    @NonNull
    ArchivePath getRootDirectory() {
        return getLocalFileSystem().getRootDirectory();
    }

    @NonNull
    ArchivePath getDefaultDirectory() {
        return getLocalFileSystem().getDefaultDirectory();
    }

    @NonNull
    Path getArchiveFile() {
        return getLocalFileSystem().getArchiveFile();
    }

    @NonNull
    ArchiveEntry getEntryAsLocal(@NonNull Path path) throws IOException {
        return getLocalFileSystem().getEntry(path);
    }

    @NonNull
    InputStream newInputStreamAsLocal(@NonNull Path file) throws IOException {
        return getLocalFileSystem().newInputStream(file);
    }

    @NonNull
    List<Path> getDirectoryChildrenAsLocal(@NonNull Path directory) throws IOException {
        return getLocalFileSystem().getDirectoryChildren(directory);
    }

    @NonNull
    String readSymbolicLinkAsLocal(@NonNull Path link) throws IOException {
        return getLocalFileSystem().readSymbolicLink(link);
    }

    void refresh() {
        getLocalFileSystem().refresh();
        getRootFileSystem().refresh();
    }

    void doRefreshIfNeededAsRoot() throws RemoteFileSystemException {
        getRootFileSystem().doRefreshIfNeeded();
    }

    @NonNull
    @Override
    public ArchivePath getPath(@NonNull ByteString first, @NonNull ByteString... more) {
        return getLocalFileSystem().getPath(first, more);
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
