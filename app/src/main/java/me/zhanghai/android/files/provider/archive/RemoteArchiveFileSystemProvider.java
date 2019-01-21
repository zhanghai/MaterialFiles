/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.AccessMode;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.FileSystemCache;
import me.zhanghai.android.files.provider.remote.RemoteFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteUtils;

public class RemoteArchiveFileSystemProvider extends RemoteFileSystemProvider {

    static final String SCHEME = RemoteUtils.toRemoteScheme(ArchiveFileSystemProvider.SCHEME);

    private static RemoteArchiveFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    @NonNull
    private final FileSystemCache<Path, RemoteArchiveFileSystem> mFileSystems =
            new FileSystemCache<>();

    @NonNull
    private final Set<RemoteArchiveFileSystem> mRefreshFileSystems = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    private RemoteArchiveFileSystemProvider() {}

    public static void install() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new RemoteArchiveFileSystemProvider();
            FileSystemProvider.installProvider(sInstance);
        }
    }

    public static boolean isRemoteArchivePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof RemoteArchivePath;
    }

    @NonNull
    public static Path getArchiveFile(@NonNull Path path) {
        requireRemoteArchivePath(path);
        RemoteArchiveFileSystem fileSystem = (RemoteArchiveFileSystem) path.getFileSystem();
        return fileSystem.getArchiveFile();
    }

    public static void refresh(@NonNull Path path) {
        requireRemoteArchivePath(path);
        RemoteArchiveFileSystem fileSystem = (RemoteArchiveFileSystem) path.getFileSystem();
        sInstance.mRefreshFileSystems.add(fileSystem);
    }

    @NonNull
    public static Path getRootPathForArchiveFile(@NonNull Path archiveFile) {
        return getOrNewFileSystem(archiveFile).getRootDirectory();
    }

    @NonNull
    static RemoteArchiveFileSystem getOrNewFileSystem(@NonNull Path archiveFile) {
        return sInstance.mFileSystems.getOrNew(archiveFile, () -> sInstance.newFileSystem(
                archiveFile));
    }

    @NonNull
    @Override
    public String getScheme() {
        return SCHEME;
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull URI uri, @NonNull Map<String, ?> env) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Objects.requireNonNull(env);
        Path archiveFile = getArchiveFileFromUri(uri);
        return mFileSystems.new_(archiveFile, () -> newFileSystem(archiveFile));
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Path archiveFile = getArchiveFileFromUri(uri);
        return mFileSystems.get(archiveFile);
    }

    @NonNull
    private static Path getArchiveFileFromUri(@NonNull URI uri) {
        URI archiveUri;
        try {
            archiveUri = new URI(uri.getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return Paths.get(archiveUri);
    }

    @Override
    protected void removeFileSystem(@NonNull FileSystem fileSystem) {
        RemoteArchiveFileSystem remoteArchiveFileSystem = (RemoteArchiveFileSystem) fileSystem;
        mFileSystems.remove(remoteArchiveFileSystem.getArchiveFile(), remoteArchiveFileSystem);
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        return getFileSystem(uri).getPath(uri.getFragment());
    }

    private static void requireSameScheme(@NonNull URI uri) {
        if (!Objects.equals(uri.getScheme(), SCHEME)) {
            throw new IllegalArgumentException("URI scheme must be \"" + SCHEME + "\"");
        }
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull Path file, @NonNull Map<String, ?> env) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(env);
        return newFileSystem(file);
    }

    @NonNull
    private RemoteArchiveFileSystem newFileSystem(@NonNull Path file) {
        return new RemoteArchiveFileSystem(this, file);
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        ensureFileSystemRemoteInterface(file);

        return super.newInputStream(file, options);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        ensureFileSystemRemoteInterface(directory);

        return super.newDirectoryStream(directory, filter);
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        ensureFileSystemRemoteInterface(link);

        return super.readSymbolicLink(link);
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        ensureFileSystemRemoteInterface(path);

        super.checkAccess(path, modes);
    }

    private void ensureFileSystemRemoteInterface(@NonNull Path path)
            throws RemoteFileSystemException {
        requireRemoteArchivePath(path);
        RemoteArchiveFileSystem fileSystem = (RemoteArchiveFileSystem) path.getFileSystem();
        fileSystem.ensureRemoteInterface();
        if (mRefreshFileSystems.remove(fileSystem)) {
            RemoteFileService.getInstance().refreshArchiveFileSystem(fileSystem);
        }
    }

    static void refreshFileSystemIfNeeded(@NonNull Path path) throws RemoteFileSystemException {
        requireRemoteArchivePath(path);
        RemoteArchiveFileSystem fileSystem = (RemoteArchiveFileSystem) path.getFileSystem();
        if (sInstance.mRefreshFileSystems.remove(fileSystem)) {
            RemoteFileService.getInstance().refreshArchiveFileSystem(fileSystem);
        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireRemoteArchivePath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        //noinspection unchecked
        return (V) new RemoteArchiveFileAttributeView(path);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(RemoteArchiveFileAttributeView.class);
    }

    private static void requireRemoteArchivePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof RemoteArchivePath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
