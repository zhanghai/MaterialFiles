/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.Consumer;
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.provider.common.PathObservableProvider;
import me.zhanghai.android.files.provider.common.Searchable;
import me.zhanghai.android.files.util.BundleBuilder;
import me.zhanghai.android.files.util.RemoteCallback;

public class RemoteFileSystemProviderInterface extends IRemoteFileSystemProvider.Stub {

    private static final String KEY_PREFIX = RemoteFileSystemProviderInterface.class.getName()
            + '.';

    static final String KEY_IO_EXCEPTION = KEY_PREFIX + "IO_EXCEPTION";

    @NonNull
    private final FileSystemProvider mProvider;

    @NonNull
    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();

    public RemoteFileSystemProviderInterface(@NonNull FileSystemProvider provider) {
        mProvider = provider;
    }

    @NonNull
    @Override
    public RemoteInputStream newInputStream(@NonNull ParcelableObject parcelableFile,
                                            @NonNull ParcelableSerializable parcelableOptions,
                                            @NonNull ParcelableException exception) {
        Path file = parcelableFile.get();
        OpenOption[] options = parcelableOptions.get();
        RemoteInputStream remoteInputStream;
        try {
            InputStream inputStream = mProvider.newInputStream(file, options);
            remoteInputStream = new RemoteInputStream(inputStream);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return remoteInputStream;
    }

    @NonNull
    @Override
    public RemoteSeekableByteChannel newByteChannel(
            @NonNull ParcelableObject parcelableFile,
            @NonNull ParcelableSerializable parcelableOptions,
            @NonNull ParcelableFileAttributes parcelableAttributes,
            @NonNull ParcelableException exception) {
        Path file = parcelableFile.get();
        Set<? extends OpenOption> options = parcelableOptions.get();
        FileAttribute<?>[] attributes = parcelableAttributes.get();
        RemoteSeekableByteChannel remoteChannel;
        try {
            SeekableByteChannel channel = mProvider.newByteChannel(file, options, attributes);
            remoteChannel = new RemoteSeekableByteChannel(channel);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return remoteChannel;
    }

    @Override
    public ParcelableDirectoryStream newDirectoryStream(
            @NonNull ParcelableObject parcelableDirectory,
            @NonNull ParcelableObject parcelableFilter,
            @NonNull ParcelableException exception) {
        Path directory = parcelableDirectory.get();
        DirectoryStream.Filter<? super Path> filter = parcelableFilter.get();
        ParcelableDirectoryStream parcelableDirectoryStream;
        try (DirectoryStream<Path> directoryStream = mProvider.newDirectoryStream(directory,
                filter)) {
            parcelableDirectoryStream = new ParcelableDirectoryStream(directoryStream);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return parcelableDirectoryStream;
    }

    @Override
    public void createDirectory(@NonNull ParcelableObject parcelableDirectory,
                                @NonNull ParcelableFileAttributes parcelableAttributes,
                                @NonNull ParcelableException exception) {
        Path directory = parcelableDirectory.get();
        FileAttribute<?>[] attributes = parcelableAttributes.get();
        try {
            mProvider.createDirectory(directory, attributes);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void createSymbolicLink(@NonNull ParcelableObject parcelableLink,
                                   @NonNull ParcelableObject parcelableTarget,
                                   @NonNull ParcelableFileAttributes parcelableAttributes,
                                   @NonNull ParcelableException exception) {
        Path link = parcelableLink.get();
        Path target = parcelableTarget.get();
        FileAttribute<?>[] attributes = parcelableAttributes.get();
        try {
            mProvider.createSymbolicLink(link, target, attributes);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void createLink(@NonNull ParcelableObject parcelableLink,
                           @NonNull ParcelableObject parcelableExisting,
                           @NonNull ParcelableException exception) {
        Path link = parcelableLink.get();
        Path existing = parcelableExisting.get();
        try {
            mProvider.createLink(link, existing);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void delete(@NonNull ParcelableObject parcelablePath,
                       @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        try {
            mProvider.delete(path);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public ParcelableObject readSymbolicLink(@NonNull ParcelableObject parcelableLink,
                                             @NonNull ParcelableException exception) {
        Path link = parcelableLink.get();
        Path target;
        try {
            target = mProvider.readSymbolicLink(link);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return new ParcelableObject(target);
    }

    @NonNull
    @Override
    public RemoteCallback copy(@NonNull ParcelableObject parcelableSource,
                               @NonNull ParcelableObject parcelableTarget,
                               @NonNull ParcelableCopyOptions parcelableOptions,
                               @NonNull RemoteCallback callback) {
        Path source = parcelableSource.get();
        Path target = parcelableTarget.get();
        CopyOption[] options = parcelableOptions.get();
        Future<Void> future = mExecutorService.submit(() -> {
            try {
                mProvider.copy(source, target, options);
                callback.sendResult(null);
            } catch (IOException e) {
                callback.sendResult(new BundleBuilder()
                        .putSerializable(KEY_IO_EXCEPTION, e)
                        .build());
            }
            return null;
        });
        return new RemoteCallback(result -> future.cancel(true));
    }

    @NonNull
    @Override
    public RemoteCallback move(@NonNull ParcelableObject parcelableSource,
                               @NonNull ParcelableObject parcelableTarget,
                               @NonNull ParcelableCopyOptions parcelableOptions,
                               @NonNull RemoteCallback callback) {
        Path source = parcelableSource.get();
        Path target = parcelableTarget.get();
        CopyOption[] options = parcelableOptions.get();
        Future<Void> future = mExecutorService.submit(() -> {
            try {
                mProvider.move(source, target, options);
                callback.sendResult(null);
            } catch (IOException e) {
                callback.sendResult(new BundleBuilder()
                        .putSerializable(KEY_IO_EXCEPTION, e)
                        .build());
            }
            return null;
        });
        return new RemoteCallback(result -> future.cancel(true));
    }

    @Override
    public boolean isSameFile(@NonNull ParcelableObject parcelablePath,
                              @NonNull ParcelableObject parcelablePath2,
                              @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        Path path2 = parcelablePath2.get();
        try {
            return mProvider.isSameFile(path, path2);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return false;
        }
    }

    @Override
    public boolean isHidden(@NonNull ParcelableObject parcelablePath,
                            @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        try {
            return mProvider.isHidden(path);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return false;
        }
    }

    @NonNull
    @Override
    public ParcelableObject getFileStore(@NonNull ParcelableObject parcelablePath,
                                         @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        FileStore fileStore;
        try {
            fileStore = mProvider.getFileStore(path);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return new ParcelableObject(fileStore);
    }

    @Override
    public void checkAccess(@NonNull ParcelableObject parcelablePath,
                            @NonNull ParcelableSerializable parcelableModes,
                            @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        AccessMode[] modes = parcelableModes.get();
        try {
            mProvider.checkAccess(path, modes);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @NonNull
    @Override
    public ParcelableObject readAttributes(@NonNull ParcelableObject parcelablePath,
                                           @NonNull ParcelableSerializable parcelableType,
                                           @NonNull ParcelableSerializable parcelableOptions,
                                           @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        Class<? extends BasicFileAttributes> type = parcelableType.get();
        LinkOption[] options = parcelableOptions.get();
        BasicFileAttributes attributes;
        try {
            attributes = mProvider.readAttributes(path, type, options);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return new ParcelableObject(attributes);
    }

    @NonNull
    @Override
    public RemotePathObservable observePath(@NonNull ParcelableObject parcelablePath,
                                            long intervalMillis,
                                            @NonNull ParcelableException exception) {
        Path path = parcelablePath.get();
        RemotePathObservable remotePathObservable;
        try {
            PathObservable pathObservable = ((PathObservableProvider) mProvider).observePath(path,
                    intervalMillis);
            remotePathObservable = new RemotePathObservable(pathObservable);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return remotePathObservable;
    }

    @NonNull
    @Override
    public RemoteCallback search(@NonNull ParcelableObject parcelableDirectory,
                                 @NonNull String query,
                                 @NonNull ParcelablePathListConsumer parcelableListener,
                                 long intervalMillis, @NonNull RemoteCallback callback) {
        Path directory = parcelableDirectory.get();
        Consumer<List<Path>> listener = parcelableListener.get();
        Future<Void> future = mExecutorService.submit(() -> {
            try {
                ((Searchable) mProvider).search(directory, query, listener, intervalMillis);
                callback.sendResult(null);
            } catch (IOException e) {
                callback.sendResult(new BundleBuilder()
                        .putSerializable(KEY_IO_EXCEPTION, e)
                        .build());
            }
            return null;
        });
        return new RemoteCallback(result -> future.cancel(true));
    }
}
