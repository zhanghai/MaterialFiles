/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.FileChannel;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.Consumer;
import java9.util.function.Function;
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.provider.common.PathObservableProvider;
import me.zhanghai.android.files.provider.common.Searchable;

public class RootableFileSystemProvider extends FileSystemProvider
        implements PathObservableProvider, Searchable {

    @NonNull
    private final FileSystemProvider mLocalProvider;
    @NonNull
    private final FileSystemProvider mRootProvider;

    public RootableFileSystemProvider(
            @NonNull Function<FileSystemProvider, FileSystemProvider> newLocalProvider,
            @NonNull Function<FileSystemProvider, FileSystemProvider> newRootProvider) {
        mLocalProvider = newLocalProvider.apply(this);
        mRootProvider = newRootProvider.apply(this);
    }

    @NonNull
    protected <FSP extends FileSystemProvider> FSP getLocalProvider() {
        //noinspection unchecked
        return (FSP) mLocalProvider;
    }

    @NonNull
    protected <FSP extends FileSystemProvider> FSP getRootProvider() {
        //noinspection unchecked
        return (FSP) mRootProvider;
    }

    @NonNull
    @Override
    public String getScheme() {
        return mLocalProvider.getScheme();
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull URI uri, @NonNull Map<String, ?> env)
            throws IOException {
        return mLocalProvider.newFileSystem(uri, env);
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        return mLocalProvider.getFileSystem(uri);
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        return mLocalProvider.getPath(uri);
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        return applyRootable(path, provider -> provider.newInputStream(path, options));
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        return applyRootable(path, provider -> provider.newOutputStream(path, options));
    }

    @NonNull
    @Override
    public FileChannel newFileChannel(@NonNull Path path,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) throws IOException {
        return applyRootable(path, provider -> provider.newFileChannel(path, options, attributes));
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path path,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes)
            throws IOException {
        return applyRootable(path, provider -> provider.newByteChannel(path, options, attributes));
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        return applyRootable(directory, provider -> provider.newDirectoryStream(directory, filter));
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        acceptRootable(directory, provider -> provider.createDirectory(directory, attributes));
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) throws IOException {
        acceptRootable(link, target, provider -> provider.createSymbolicLink(link, target,
                attributes));
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        acceptRootable(link, existing, provider -> provider.createLink(link, existing));
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        acceptRootable(path, provider -> provider.delete(path));
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        return applyRootable(link, provider -> provider.readSymbolicLink(link));
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        acceptRootable(source, target, provider -> provider.copy(source, target, options));
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        acceptRootable(source, target, provider -> provider.move(source, target, options));
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        return applyRootable(path, path2, provider -> provider.isSameFile(path, path2));
    }

    @Override
    public boolean isHidden(@NonNull Path path) throws IOException {
        return applyRootable(path, provider -> provider.isHidden(path));
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) throws IOException {
        return applyRootable(path, provider -> provider.getFileStore(path));
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        acceptRootable(path, provider -> provider.checkAccess(path, modes));
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        return mLocalProvider.getFileAttributeView(path, type, options);
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        return mLocalProvider.readAttributes(path, type, options);
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) throws IOException {
        return applyRootable(path, provider -> provider.readAttributes(path, attributes, options));
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, Object value,
                             @NonNull LinkOption... options) throws IOException {
        acceptRootable(path, provider -> provider.setAttribute(path, attribute, value, options));
    }

    @NonNull
    @Override
    public PathObservable observePath(@NonNull Path path, long intervalMillis) throws IOException {
        if (!(mLocalProvider instanceof PathObservableProvider)) {
            throw new UnsupportedOperationException();
        }
        return applyRootable(path, provider -> {
            // observePath() may or may not be able to detect denied access, and that is expansive
            // on Linux (having to create the WatchService first before registering a WatchKey). So
            // we check the access beforehand.
            if (provider == mLocalProvider) {
                BasicFileAttributes attributes = null;
                try {
                    attributes = provider.readAttributes(path, BasicFileAttributes.class);
                } catch (IOException ignored) {}
                if (attributes == null) {
                    attributes = provider.readAttributes(path, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS);
                }
                if (attributes.isSymbolicLink()) {
                    provider.readSymbolicLink(path);
                } else {
                    provider.checkAccess(path, AccessMode.READ);
                }
            }
            return ((PathObservableProvider) provider).observePath(path, intervalMillis);
        });
    }

    @Override
    public void search(@NonNull Path directory, @NonNull String query,
                       @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        acceptRootable(directory, provider -> ((Searchable) provider).search(directory, query,
                listener, intervalMillis));
    }

    private void acceptRootable(@NonNull Path path,
                                RootUtils.Consumer<FileSystemProvider> consumer)
            throws IOException {
        RootUtils.acceptRootable(path, mLocalProvider, mRootProvider, consumer);
    }

    private void acceptRootable(@NonNull Path path1, @NonNull Path path2,
                                RootUtils.Consumer<FileSystemProvider> consumer)
            throws IOException {
        RootUtils.acceptRootable(path1, path2, mLocalProvider, mRootProvider, consumer);
    }

    private <R> R applyRootable(@NonNull Path path,
                                 RootUtils.Function<FileSystemProvider, R> function)
            throws IOException {
        return RootUtils.applyRootable(path, mLocalProvider, mRootProvider, function);
    }

    private <R> R applyRootable(@NonNull Path path1, @NonNull Path path2,
                                @NonNull RootUtils.Function<FileSystemProvider, R> function)
            throws IOException {
        return RootUtils.applyRootable(path1, path2, mLocalProvider, mRootProvider, function);
    }
}
