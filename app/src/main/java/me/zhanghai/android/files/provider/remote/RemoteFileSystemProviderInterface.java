/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.AccessMode;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.spi.FileSystemProvider;

public class RemoteFileSystemProviderInterface extends IRemoteFileSystemProvider.Stub {

    @NonNull
    private final FileSystemProvider mProvider;

    public RemoteFileSystemProviderInterface(@NonNull FileSystemProvider provider) {
        mProvider = provider;
    }

    @Override
    public ParcelableDirectoryStream newDirectoryStream(
            @NonNull ParcelablePath remoteDirectory,
            @NonNull ParcelableDirectoryStreamFilter parcelableFilter,
            @NonNull ParcelableIoException ioException) {
        Path directory = RemoteUtils.toLocalPath(remoteDirectory.get());
        DirectoryStream.Filter<? super Path> filter = parcelableFilter.get();
        try (DirectoryStream<Path> directoryStream = mProvider.newDirectoryStream(directory,
                filter)) {
            return ParcelableDirectoryStream.createForRemote(directoryStream);
        } catch (IOException e) {
            ioException.set(e);
            return null;
        }
    }

    @Override
    public ParcelablePath readSymbolicLink(@NonNull ParcelablePath remoteLink,
                                           @NonNull ParcelableIoException ioException) {
        Path link = RemoteUtils.toLocalPath(remoteLink.get());
        Path target;
        try {
            target = mProvider.readSymbolicLink(link);
        } catch (IOException e) {
            ioException.set(e);
            return null;
        }
        return new ParcelablePath(target);
    }

    @Override
    public boolean isSameFile(@NonNull ParcelablePath remotePath,
                              @NonNull ParcelablePath remotePath2,
                              @NonNull ParcelableIoException ioException) {
        Path path = RemoteUtils.toLocalPath(remotePath.get());
        Path path2 = RemoteUtils.toLocalPath(remotePath2.get());
        try {
            return mProvider.isSameFile(path, path2);
        } catch (IOException e) {
            ioException.set(e);
            return false;
        }
    }

    @Override
    public boolean isHidden(@NonNull ParcelablePath remotePath,
                            @NonNull ParcelableIoException ioException) {
        Path path = RemoteUtils.toLocalPath(remotePath.get());
        try {
            return mProvider.isHidden(path);
        } catch (IOException e) {
            ioException.set(e);
            return false;
        }
    }

    @NonNull
    @Override
    public ParcelableRemoteFileStore getFileStore(@NonNull ParcelablePath remotePath,
                                                  @NonNull ParcelableIoException ioException) {
        Path path = RemoteUtils.toLocalPath(remotePath.get());
        FileStore fileStore;
        try {
            fileStore = mProvider.getFileStore(path);
        } catch (IOException e) {
            ioException.set(e);
            return null;
        }
        RemoteFileStore remoteFileStore = ((RemotableFileStore) fileStore).toRemote();
        return new ParcelableRemoteFileStore(remoteFileStore);
    }

    @Override
    public void checkAccess(@NonNull ParcelablePath remotePath,
                            @NonNull ParcelableAccessModes parcelableModes,
                            @NonNull ParcelableIoException ioException) {
        Path path = RemoteUtils.toLocalPath(remotePath.get());
        AccessMode[] modes = parcelableModes.get();
        try {
            mProvider.checkAccess(path, modes);
        } catch (IOException e) {
            ioException.set(e);
        }
    }

    @NonNull
    @Override
    public ParcelableFileAttributes readAttributes(@NonNull ParcelablePath remotePath,
                                                   @NonNull ParcelableClass parcelableType,
                                                   @NonNull ParcelableLinkOptions parcelableOptions,
                                                   @NonNull ParcelableIoException ioException) {
        Path path = RemoteUtils.toLocalPath(remotePath.get());
        Class<? extends BasicFileAttributes> type = parcelableType.get();
        LinkOption[] options = parcelableOptions.get();
        BasicFileAttributes attributes;
        try {
            attributes = mProvider.readAttributes(path, type, options);
        } catch (IOException e) {
            ioException.set(e);
            return null;
        }
        return new ParcelableFileAttributes(attributes);
    }
}
