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
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.Files;
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
import me.zhanghai.android.files.util.RemoteCallback;
import me.zhanghai.java.promise.Promise;

public abstract class RemoteFileSystemProvider extends FileSystemProvider
        implements PathObservableProvider, Searchable {

    private final RemoteInterfaceHolder<IRemoteFileSystemProvider> mRemoteInterface;

    public RemoteFileSystemProvider(
            @NonNull RemoteInterfaceHolder<IRemoteFileSystemProvider> remoteInterface) {
        mRemoteInterface = remoteInterface;
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        ParcelableObject parcelableFile = new ParcelableObject(file);
        ParcelableSerializable parcelableOptions = new ParcelableSerializable(options);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemoteInputStream remoteInputStream;
        try {
            remoteInputStream = remoteInterface.newInputStream(parcelableFile, parcelableOptions,
                    exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return remoteInputStream;
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
        ParcelableObject parcelableFile = new ParcelableObject(file);
        Serializable serializableOptions = options instanceof Serializable ? (Serializable) options
                : new HashSet<>(options);
        ParcelableSerializable parcelableOptions = new ParcelableSerializable(serializableOptions);
        ParcelableFileAttributes parcelableAttributes = new ParcelableFileAttributes(attributes);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemoteSeekableByteChannel remoteChannel;
        try {
            remoteChannel = remoteInterface.newByteChannel(parcelableFile, parcelableOptions,
                    parcelableAttributes, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return remoteChannel;
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
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableDirectoryStream parcelableDirectoryStream;
        try {
            parcelableDirectoryStream = remoteInterface.newDirectoryStream(parcelableDirectory,
                    parcelableFilter, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return parcelableDirectoryStream.get();
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        ParcelableObject parcelableDirectory = new ParcelableObject(directory);
        ParcelableFileAttributes parcelableAttributes = new ParcelableFileAttributes(attributes);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.createDirectory(parcelableDirectory, parcelableAttributes, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) throws IOException {
        ParcelableObject parcelableLink = new ParcelableObject(link);
        ParcelableObject parcelableTarget = new ParcelableObject(target);
        ParcelableFileAttributes parcelableAttributes = new ParcelableFileAttributes(attributes);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.createSymbolicLink(parcelableLink, parcelableTarget,
                    parcelableAttributes, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        ParcelableObject parcelableLink = new ParcelableObject(link);
        ParcelableObject parcelableExisting = new ParcelableObject(existing);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.createLink(parcelableLink, parcelableExisting, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.delete(parcelablePath, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        ParcelableObject parcelableLink = new ParcelableObject(link);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableTarget;
        try {
            parcelableTarget = remoteInterface.readSymbolicLink(parcelableLink, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return parcelableTarget.get();
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        ParcelableObject parcelableSource = new ParcelableObject(source);
        ParcelableObject parcelableTarget = new ParcelableObject(target);
        ParcelableCopyOptions parcelableOptions = new ParcelableCopyOptions(options);
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemoteCallback[] interruptible = new RemoteCallback[1];
        Promise<Void> promise = new Promise<>(settler -> {
            RemoteCallback callback = new RemoteCallback(result -> {
                if (result == null) {
                    settler.resolve(null);
                } else {
                    IOException exception = (IOException) result.getSerializable(
                            RemoteFileSystemProviderInterface.KEY_IO_EXCEPTION);
                    settler.reject(exception);
                }
            });
            try {
                interruptible[0] = remoteInterface.copy(parcelableSource, parcelableTarget,
                        parcelableOptions, callback);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
        });
        try {
            promise.await();
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        } catch (InterruptedException e) {
            interruptible[0].sendResult(null);
            InterruptedIOException exception = new InterruptedIOException();
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException  {
        ParcelableObject parcelableSource = new ParcelableObject(source);
        ParcelableObject parcelableTarget = new ParcelableObject(target);
        ParcelableCopyOptions parcelableOptions = new ParcelableCopyOptions(options);
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemoteCallback[] interruptible = new RemoteCallback[1];
        Promise<Void> promise = new Promise<>(settler -> {
            RemoteCallback callback = new RemoteCallback(result -> {
                if (result == null) {
                    settler.resolve(null);
                } else {
                    IOException exception = (IOException) result.getSerializable(
                            RemoteFileSystemProviderInterface.KEY_IO_EXCEPTION);
                    settler.reject(exception);
                }
            });
            try {
                interruptible[0] = remoteInterface.move(parcelableSource, parcelableTarget,
                        parcelableOptions, callback);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
        });
        try {
            promise.await();
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        } catch (InterruptedException e) {
            interruptible[0].sendResult(null);
            InterruptedIOException exception = new InterruptedIOException();
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableObject parcelablePath2 = new ParcelableObject(path2);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        boolean isSameFile;
        try {
            isSameFile = remoteInterface.isSameFile(parcelablePath, parcelablePath2, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return isSameFile;
    }

    @Override
    public boolean isHidden(@NonNull Path path) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        boolean isHidden;
        try {
            isHidden = remoteInterface.isHidden(parcelablePath, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return isHidden;
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableFileStore;
        try {
            parcelableFileStore = remoteInterface.getFileStore(parcelablePath, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return parcelableFileStore.get();
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableSerializable parcelableModes = new ParcelableSerializable(modes);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.checkAccess(parcelablePath, parcelableModes, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableSerializable parcelableType = new ParcelableSerializable(type);
        ParcelableSerializable parcelableOptions = new ParcelableSerializable(options);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableAttributes;
        try {
            parcelableAttributes = remoteInterface.readAttributes(parcelablePath, parcelableType,
                    parcelableOptions, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
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

    @NonNull
    @Override
    public PathObservable observePath(@NonNull Path path, long intervalMillis) throws IOException {
        ParcelableObject parcelablePath = new ParcelableObject(path);
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemotePathObservable remotePathObservable;
        try {
            remotePathObservable = remoteInterface.observePath(parcelablePath, intervalMillis,
                    exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        remotePathObservable.initForRemote();
        return remotePathObservable;
    }

    @Override
    public void search(@NonNull Path directory, @NonNull String query,
                       @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        ParcelableObject parcelableDirectory = new ParcelableObject(directory);
        ParcelablePathListConsumer parcelableListener = new ParcelablePathListConsumer(listener);
        IRemoteFileSystemProvider remoteInterface = mRemoteInterface.get();
        RemoteCallback[] interruptible = new RemoteCallback[1];
        Promise<Void> promise = new Promise<>(settler -> {
            RemoteCallback callback = new RemoteCallback(result -> {
                if (result == null) {
                    settler.resolve(null);
                } else {
                    IOException exception = (IOException) result.getSerializable(
                            RemoteFileSystemProviderInterface.KEY_IO_EXCEPTION);
                    settler.reject(exception);
                }
            });
            try {
                interruptible[0] = remoteInterface.search(parcelableDirectory, query,
                        parcelableListener, intervalMillis, callback);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
        });
        try {
            promise.await();
        } catch (ExecutionException e) {
            throw (IOException) e.getCause();
        } catch (InterruptedException e) {
            interruptible[0].sendResult(null);
            InterruptedIOException exception = new InterruptedIOException();
            exception.initCause(e);
            throw exception;
        }
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
