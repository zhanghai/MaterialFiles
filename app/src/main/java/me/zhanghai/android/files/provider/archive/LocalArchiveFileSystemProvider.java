/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.FileChannel;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.Consumer;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringPath;
import me.zhanghai.android.files.provider.common.ByteStringUriUtils;
import me.zhanghai.android.files.provider.common.FileSystemCache;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.common.PathListDirectoryStream;
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException;
import me.zhanghai.android.files.provider.common.Searchable;
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable;

class LocalArchiveFileSystemProvider extends FileSystemProvider implements Searchable {

    static final String SCHEME = "archive";

    private ArchiveFileSystemProvider mProvider;

    @NonNull
    private final FileSystemCache<Path, ArchiveFileSystem> mFileSystems = new FileSystemCache<>();

    LocalArchiveFileSystemProvider(@NonNull ArchiveFileSystemProvider provider) {
        mProvider = provider;
    }

    static boolean isArchivePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof ArchivePath;
    }

    @NonNull
    static Path getArchiveFile(@NonNull Path path) {
        requireArchivePath(path);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        return fileSystem.getArchiveFile();
    }

    static void refresh(@NonNull Path path) {
        requireArchivePath(path);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        fileSystem.refresh();
    }

    @NonNull
    Path getRootPathForArchiveFile(@NonNull Path archiveFile) {
        return getOrNewFileSystem(archiveFile).getRootDirectory();
    }

    @NonNull
    ArchiveFileSystem getOrNewFileSystem(@NonNull Path archiveFile) {
        Objects.requireNonNull(archiveFile);
        return mFileSystems.getOrNew(archiveFile, () -> newFileSystem(archiveFile));
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
    public ArchiveFileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Path archiveFile = getArchiveFileFromUri(uri);
        return mFileSystems.get(archiveFile);
    }

    @NonNull
    private static Path getArchiveFileFromUri(@NonNull URI uri) {
        ByteString schemeSpecificPart = ByteStringUriUtils.getDecodedSchemeSpecificPart(uri);
        if (schemeSpecificPart == null) {
            throw new IllegalArgumentException("URI must have a scheme specific part");
        }
        URI archiveUri;
        try {
            archiveUri = new URI(schemeSpecificPart.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return Paths.get(archiveUri);
    }

    void removeFileSystem(@NonNull ArchiveFileSystem fileSystem) {
        mFileSystems.remove(fileSystem.getArchiveFile(), fileSystem);
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Path archiveFile = getArchiveFileFromUri(uri);
        ByteString fragment = ByteStringUriUtils.getDecodedFragment(uri);
        if (fragment == null) {
            throw new IllegalArgumentException("URI must have a fragment");
        }
        return getOrNewFileSystem(archiveFile).getPath(fragment);
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
    private ArchiveFileSystem newFileSystem(@NonNull Path archiveFile) {
        return new ArchiveFileSystem(mProvider, archiveFile);
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        requireArchivePath(file);
        Objects.requireNonNull(options);
        OpenOptions openOptions = OpenOptions.fromArray(options);
        ArchiveOpenOptions.check(openOptions);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) file.getFileSystem();
        return fileSystem.newInputStreamAsLocal(file);
    }

    @NonNull
    @Override
    public FileChannel newFileChannel(@NonNull Path file,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) {
        requireArchivePath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        OpenOptions openOptions = OpenOptions.fromSet(options);
        ArchiveOpenOptions.check(openOptions);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path file,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes) {
        requireArchivePath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        OpenOptions openOptions = OpenOptions.fromSet(options);
        ArchiveOpenOptions.check(openOptions);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        requireArchivePath(directory);
        Objects.requireNonNull(filter);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) directory.getFileSystem();
        List<Path> children = fileSystem.getDirectoryChildrenAsLocal(directory);
        return new PathListDirectoryStream(children, filter);
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException{
        requireArchivePath(directory);
        Objects.requireNonNull(attributes);
        throw new ReadOnlyFileSystemException(directory.toString());
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) throws IOException {
        requireArchivePath(link);
        requireArchiveOrByteStringPath(target);
        Objects.requireNonNull(attributes);
        throw new ReadOnlyFileSystemException(link.toString(), target.toString(), null);
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        requireArchivePath(link);
        requireArchivePath(existing);
        throw new ReadOnlyFileSystemException(link.toString(), existing.toString(), null);
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        requireArchivePath(path);
        throw new ReadOnlyFileSystemException(path.toString());
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        requireArchivePath(link);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) link.getFileSystem();
        String target = fileSystem.readSymbolicLinkAsLocal(link);
        return new ByteStringPath(ByteString.fromString(target));
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        requireArchivePath(source);
        requireArchivePath(target);
        Objects.requireNonNull(options);
        throw new ReadOnlyFileSystemException(source.toString(), target.toString(), null);
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        requireArchivePath(source);
        requireArchivePath(target);
        Objects.requireNonNull(options);
        throw new ReadOnlyFileSystemException(source.toString(), target.toString(), null);
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        requireArchivePath(path);
        Objects.requireNonNull(path2);
        if (Objects.equals(path, path2)) {
            return true;
        }
        if (!(path2 instanceof ArchivePath)) {
            return false;
        }
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        ArchiveFileSystem fileSystem2 = (ArchiveFileSystem) path2.getFileSystem();
        if (!Files.isSameFile(fileSystem.getArchiveFile(), fileSystem2.getArchiveFile())) {
            return false;
        }
        return Objects.equals(path, fileSystem.getPath(path2.toString()));
    }

    @Override
    public boolean isHidden(@NonNull Path path) {
        requireArchivePath(path);
        return false;
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) {
        requireArchivePath(path);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        Path archiveFile = fileSystem.getArchiveFile();
        return new ArchiveFileStore(archiveFile);
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        requireArchivePath(path);
        Objects.requireNonNull(modes);
        AccessModes accessModes = AccessModes.fromArray(modes);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        fileSystem.getEntryAsLocal(path);
        if (accessModes.hasWrite() || accessModes.hasExecute()) {
            throw new AccessDeniedException(path.toString());
        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireArchivePath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        //noinspection unchecked
        return (V) getFileAttributeView(path);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(ArchiveFileAttributeView.class);
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        requireArchivePath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!type.isAssignableFrom(ArchiveFileAttributes.class)) {
            throw new UnsupportedOperationException(type.toString());
        }
        //noinspection unchecked
        return (A) getFileAttributeView(path).readAttributes();
    }

    private static ArchiveFileAttributeView getFileAttributeView(@NonNull Path path) {
        return new ArchiveFileAttributeView(path);
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) {
        requireArchivePath(path);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) {
        requireArchivePath(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void search(@NonNull Path directory, @NonNull String query,
                       @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        requireArchivePath(directory);
        Objects.requireNonNull(query);
        Objects.requireNonNull(listener);
        WalkFileTreeSearchable.search(directory, query, listener, intervalMillis);
    }

    private static void requireArchivePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof ArchivePath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }

    private static ByteString requireArchiveOrByteStringPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (path instanceof ArchivePath) {
            return ((ArchivePath) path).toByteString();
        } else if (path instanceof ByteStringPath) {
            return ((ByteStringPath) path).toByteString();
        } else {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
