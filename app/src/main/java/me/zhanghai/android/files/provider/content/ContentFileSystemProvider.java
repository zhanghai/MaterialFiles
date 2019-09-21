/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
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
import java8.nio.file.FileSystemAlreadyExistsException;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.StandardOpenOption;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.Objects;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringPath;
import me.zhanghai.android.files.provider.common.MoreFileChannels;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;

public class ContentFileSystemProvider extends FileSystemProvider {

    static final String SCHEME = ContentResolver.SCHEME_CONTENT;

    private static ContentFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    @NonNull
    private final ContentFileSystem mFileSystem;

    private ContentFileSystemProvider() {
        mFileSystem = new ContentFileSystem(this);
    }

    public static void install() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new ContentFileSystemProvider();
            FileSystemProvider.installProvider(sInstance);
        }
    }

    public static boolean isContentPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof ContentPath;
    }

    @NonNull
    static ContentFileSystem getFileSystem() {
        return sInstance.mFileSystem;
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
        throw new FileSystemAlreadyExistsException();
    }

    @NonNull
    @Override
    public ContentFileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        return mFileSystem;
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        return mFileSystem.getPath(uri.toString());
    }

    private static void requireSameScheme(@NonNull URI uri) {
        if (!Objects.equals(uri.getScheme(), SCHEME)) {
            throw new IllegalArgumentException("URI scheme must be \"" + SCHEME + "\"");
        }
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        ContentPath contentFile = requireContentPath(file);
        Objects.requireNonNull(options);
        OpenOptions openOptions = OpenOptions.fromArray(options);
        if (openOptions.hasWrite()) {
            throw new UnsupportedOperationException(StandardOpenOption.WRITE.toString());
        }
        if (openOptions.hasAppend()) {
            throw new UnsupportedOperationException(StandardOpenOption.APPEND.toString());
        }
        String mode = ContentOpenOptions.toMode(openOptions);
        try {
            return Resolver.openInputStream(contentFile.getUri(), mode);
        } catch (ResolverException e) {
            throw e.toFileSystemException(file.toString());
        }
    }

    @NonNull
    @Override
    public OutputStream newOutputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        ContentPath contentFile = requireContentPath(file);
        Objects.requireNonNull(options);
        Set<OpenOption> optionsSet = new HashSet<>(Arrays.asList(options));
        if (optionsSet.isEmpty()) {
            optionsSet.add(StandardOpenOption.CREATE);
            optionsSet.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        optionsSet.add(StandardOpenOption.WRITE);
        OpenOptions openOptions = OpenOptions.fromSet(optionsSet);
        String mode = ContentOpenOptions.toMode(openOptions);
        try {
            return Resolver.openOutputStream(contentFile.getUri(), mode);
        } catch (ResolverException e) {
            throw e.toFileSystemException(file.toString());
        }
    }

    @NonNull
    @Override
    public FileChannel newFileChannel(@NonNull Path file,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) throws IOException {
        ContentPath contentFile = requireContentPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        OpenOptions openOptions = OpenOptions.fromSet(options);
        String mode = ContentOpenOptions.toMode(openOptions);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        ParcelFileDescriptor pfd;
        try {
            pfd = Resolver.openParcelFileDescriptor(contentFile.getUri(), mode);
        } catch (ResolverException e) {
            throw e.toFileSystemException(file.toString());
        }
        return MoreFileChannels.open(pfd, mode);
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path file,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes)
            throws IOException {
        requireContentPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        return newFileChannel(file, options, attributes);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter) {
        requireContentPath(directory);
        Objects.requireNonNull(filter);
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes) {
        requireContentPath(directory);
        Objects.requireNonNull(attributes);
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) {
        requireContentPath(link);
        requireContentOrByteStringPath(target);
        Objects.requireNonNull(attributes);
        throw new UnsupportedOperationException();
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) {
        requireContentPath(link);
        requireContentPath(existing);
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        ContentPath contentPath = requireContentPath(path);
        try {
            Resolver.delete(contentPath.getUri());
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) {
        requireContentPath(link);
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options) {
        requireContentPath(source);
        requireContentPath(target);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options) {
        requireContentPath(source);
        requireContentPath(target);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) {
        requireContentPath(path);
        Objects.requireNonNull(path2);
        return Objects.equals(path, path2);
    }

    @Override
    public boolean isHidden(@NonNull Path path) {
        requireContentPath(path);
        return false;
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) {
        requireContentPath(path);
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        ContentPath contentPath = requireContentPath(path);
        Objects.requireNonNull(modes);
        AccessModes accessModes = AccessModes.fromArray(modes);
        if (accessModes.hasExecute()) {
            throw new AccessDeniedException(path.toString());
        }
        if (accessModes.hasWrite()) {
            try (OutputStream outputStream = Resolver.openOutputStream(contentPath.getUri(), "w")) {
                // Do nothing.
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
        if (accessModes.hasRead()) {
            try (InputStream inputStream = Resolver.openInputStream(contentPath.getUri(), "r")) {
                // Do nothing.
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
        if (!(accessModes.hasRead() || accessModes.hasWrite())) {
            try {
                Resolver.checkExistence(contentPath.getUri());
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireContentPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        //noinspection unchecked
        return (V) getFileAttributeView(path);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(ContentFileAttributeView.class);
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        requireContentPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!type.isAssignableFrom(ContentFileAttributes.class)) {
            throw new UnsupportedOperationException(type.toString());
        }
        //noinspection unchecked
        return (A) getFileAttributeView(path).readAttributes();
    }

    private static ContentFileAttributeView getFileAttributeView(@NonNull Path path) {
        ContentPath contentPath = requireContentPath(path);
        return new ContentFileAttributeView(contentPath);
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) {
        requireContentPath(path);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) {
        requireContentPath(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    private static ContentPath requireContentPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof ContentPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (ContentPath) path;
    }

    private static ByteString requireContentOrByteStringPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (path instanceof ContentPath) {
            return ((ContentPath) path).toByteString();
        } else if (path instanceof ByteStringPath) {
            return ((ByteStringPath) path).toByteString();
        } else {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
