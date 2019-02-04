/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.spi.FileSystemProvider;

public abstract class RemoteFileSystemProvider extends FileSystemProvider {

    private final RemoteInterfaceHolder<IRemoteFileSystemProvider> mRemoteInterface;

    public RemoteFileSystemProvider(@NonNull String scheme) {
        mRemoteInterface = new RemoteInterfaceHolder<>(() -> RemoteFileService.getInstance()
                .getRemoteFileSystemProviderInterface(scheme));
    }

    protected abstract void removeFileSystem(@NonNull FileSystem fileSystem);

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public FileChannel newFileChannel(@NonNull Path file,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path file,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        ParcelableObject parcelableDirectory = new ParcelableObject(directory);
        if (!(filter instanceof Parcelable)) {
            if (filter.getClass().getEnclosingClass() == Files.class) {
                // Allow Files.AcceptAllFilter, but make it Parcelable.
                filter = ParcelableAcceptAllDirectoryStreamFilter.getInstance();
            } else {
                throw new IllegalArgumentException("filter is not Parcelable");
            }
        }
        ParcelableObject parcelableFilter = new ParcelableObject(filter);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableDirectoryStream parcelableDirectoryStream;
        try {
            parcelableDirectoryStream = remoteInterface.newDirectoryStream(parcelableDirectory,
                    parcelableFilter, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return parcelableDirectoryStream.get();
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        ParcelableObject parcelableLink = new ParcelableObject(link);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableTarget;
        try {
            parcelableTarget = remoteInterface.readSymbolicLink(parcelableLink, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return parcelableTarget.get();
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException  {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableObject parcelablePath2 = new ParcelableObject(path2);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        boolean isSameFile;
        try {
            isSameFile = remoteInterface.isSameFile(parcelablePath, parcelablePath2, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return isSameFile;
    }

    @Override
    public boolean isHidden(@NonNull Path path) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        boolean isHidden;
        try {
            isHidden = remoteInterface.isHidden(parcelablePath, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return isHidden;
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableFileStore;
        try {
            parcelableFileStore = remoteInterface.getFileStore(parcelablePath, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return parcelableFileStore.get();
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableAccessModes parcelableModes = new ParcelableAccessModes(modes);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.checkAccess(parcelablePath, parcelableModes, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        SerializableObject serializableType = new SerializableObject(type);
        ParcelableLinkOptions parcelableOptions = new ParcelableLinkOptions(options);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableAttributes;
        try {
            parcelableAttributes = remoteInterface.readAttributes(parcelablePath, serializableType,
                    parcelableOptions, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return parcelableAttributes.get();
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }


    private static class ParcelableAcceptAllDirectoryStreamFilter
            implements DirectoryStream.Filter<Path>, Parcelable {

        private static final ParcelableAcceptAllDirectoryStreamFilter sInstance =
                new ParcelableAcceptAllDirectoryStreamFilter();

        private ParcelableAcceptAllDirectoryStreamFilter() {}

        public static ParcelableAcceptAllDirectoryStreamFilter getInstance() {
            return sInstance;
        }

        @Override
        public boolean accept(@NonNull Path entry) {
            return true;
        }


        public static final Creator<ParcelableAcceptAllDirectoryStreamFilter> CREATOR =
                new Creator<ParcelableAcceptAllDirectoryStreamFilter>() {
                    @Override
                    public ParcelableAcceptAllDirectoryStreamFilter createFromParcel(
                            Parcel source) {
                        return sInstance;
                    }
                    @Override
                    public ParcelableAcceptAllDirectoryStreamFilter[] newArray(int size) {
                        return new ParcelableAcceptAllDirectoryStreamFilter[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {}
    }
}
