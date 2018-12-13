/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
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
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LinuxFileSystemProvider extends FileSystemProvider {

    private static final String SCHEME = "file";

    private static final String HIDDEN_FILE_NAME_PREFIX = ".";

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
    public FileChannel newFileChannel(@NonNull Path file,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        requireLinuxPath(file);
        String path = file.toString();
        OpenOptions openOptions = OpenOptions.fromSet(options);
        int flags = LinuxOpenOptions.toFlags(openOptions);
        int mode = LinuxFileMode.toInt(LinuxFileMode.fromAttributes(attributes,
                LinuxFileMode.DEFAULT_MODE_CREATE_FILE));
        FileDescriptor fd;
        try {
            fd = Syscalls.open(path, flags, mode);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path);
        }
        FileChannel fileChannel = LinuxFileChannels.open(fd, flags);
        if (openOptions.hasDeleteOnClose()) {
            try {
                Syscalls.remove(path);
            } catch (SyscallException e) {
                e.printStackTrace();
            }
        }
        return fileChannel;
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path file,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes)
            throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        return newFileChannel(file, options, attributes);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(filter);
        requireLinuxPath(directory);
        String path = directory.toString();
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(attributes);
        requireLinuxPath(directory);
        String path = directory.toString();
        int mode = LinuxFileMode.toInt(LinuxFileMode.fromAttributes(attributes,
                LinuxFileMode.DEFAULT_MODE_CREATE_DIRECTORY));
        try {
            Syscalls.mkdir(path, mode);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path);
        }
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attrs) throws IOException {
        Objects.requireNonNull(link);
        Objects.requireNonNull(target);
        Objects.requireNonNull(attrs);
        if (attrs.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attrs));
        }
        requireLinuxPath(target);
        String targetString = target.toString();
        requireLinuxPath(link);
        String linkPath = link.toString();
        try {
            Syscalls.symlink(targetString, linkPath);
        } catch (SyscallException e) {
            throw e.toFileSystemException(linkPath, targetString);
        }
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        Objects.requireNonNull(link);
        Objects.requireNonNull(existing);
        requireLinuxPath(existing);
        String oldPath = existing.toString();
        requireLinuxPath(link);
        String newPath = link.toString();
        try {
            Syscalls.link(oldPath, newPath);
        } catch (SyscallException e) {
            throw e.toFileSystemException(newPath, oldPath);
        }
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        requireLinuxPath(path);
        String pathString = path.toString();
        try {
            Syscalls.remove(pathString);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathString);
        }
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        Objects.requireNonNull(link);
        requireLinuxPath(link);
        String path = link.toString();
        String target;
        try {
            target = Syscalls.readlink(path);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path);
        }
        return mFileSystem.getPath(target);
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(options);
        requireLinuxPath(source);
        String sourceString = source.toString();
        requireLinuxPath(target);
        String targetString = target.toString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMoveFiles.copy(sourceString, targetString, copyOptions);
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(options);
        requireLinuxPath(source);
        String sourceString = source.toString();
        requireLinuxPath(target);
        String targetString = target.toString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMoveFiles.move(sourceString, targetString, copyOptions);
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
    public boolean isHidden(@NonNull Path path) {
        Objects.requireNonNull(path);
        requireLinuxPath(path);
        String fileName = path.getFileName().toString();
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX);
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
        requireLinuxPath(path);
        String pathString = path.toString();
        AccessModes accessModes = AccessModes.fromArray(modes);
        int mode;
        if (!(accessModes.hasRead() || accessModes.hasWrite() || accessModes.hasExecute())) {
            mode = OsConstants.F_OK;
        } else {
            mode = 0;
            if (accessModes.hasRead()) {
                mode |= OsConstants.R_OK;
            }
            if (accessModes.hasWrite()) {
                mode |= OsConstants.W_OK;
            }
            if (accessModes.hasExecute()) {
                mode |= OsConstants.X_OK;
            }
        }
        boolean accessible;
        try {
            // TODO: Should use euidaccess() but that's unavailable on Android.
            accessible = Syscalls.access(pathString, mode);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathString);
        }
        if (!accessible) {
            throw new AccessDeniedException(pathString);
        }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    private static void requireLinuxPath(@NonNull Path path) {
        if (!(path instanceof LinuxPath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
