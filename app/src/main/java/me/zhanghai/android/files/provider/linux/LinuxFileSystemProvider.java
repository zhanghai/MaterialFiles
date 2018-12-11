/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.FileSystemAlreadyExistsException;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LinuxFileSystemProvider extends FileSystemProvider {

    private static final String SCHEME = "file";

    @NonNull
    private final LinuxFileSystem mFileSystem = new LinuxFileSystem(this);

    public static void installAsDefault() {
        FileSystemProvider.installDefaultProvider(new LinuxFileSystemProvider());
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
        Objects.requireNonNull(env);
        requireSameScheme(uri);
        throw new FileSystemAlreadyExistsException();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        return mFileSystem;
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("URI must have a path");
        }
        return mFileSystem.getPath(path);
    }

    private static void requireSameScheme(@NonNull URI uri) {
        if (!Objects.equals(uri.getScheme(), SCHEME)) {
            throw new IllegalArgumentException("URI scheme must be \"" + SCHEME + "\"");
        }
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path path,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attrs)
            throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attrs);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path dir, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(filter);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(@NonNull Path dir, @NonNull FileAttribute<?>... attrs)
            throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        try {
            // FIXME: Use attrs
            Syscalls.mkdir(dir.toString(), OsConstants.S_IRWXU | OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        } catch (SyscallException e) {
            e.rethrowAsFileSystemException(dir.toString(), null);
        }
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(path2);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(modes);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }
}
