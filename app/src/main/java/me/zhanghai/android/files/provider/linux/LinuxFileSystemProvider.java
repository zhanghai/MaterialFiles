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
import java8.nio.file.FileSystemException;
import java8.nio.file.LinkOption;
import java8.nio.file.NotLinkException;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.common.LinkOptions;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.StringPath;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LinuxFileSystemProvider extends FileSystemProvider {

    static final String SCHEME = "file";

    private static final String HIDDEN_FILE_NAME_PREFIX = ".";

    private static LinuxFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    @NonNull
    private final LinuxFileSystem mFileSystem = new LinuxFileSystem(this);

    private LinuxFileSystemProvider() {}

    public static void installAsDefault() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new LinuxFileSystemProvider();
            FileSystemProvider.installDefaultProvider(sInstance);
        }
    }

    public static boolean isLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof LinuxPath;
    }

    @NonNull
    static LinuxFileSystem getFileSystem() {
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
        requireLinuxPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        String path = file.toString();
        OpenOptions openOptions = OpenOptions.fromSet(options);
        int flags = LinuxOpenOptions.toFlags(openOptions);
        int mode = PosixFileMode.toInt(PosixFileMode.fromAttributes(attributes,
                PosixFileMode.DEFAULT_MODE_CREATE_FILE));
        FileDescriptor fd;
        try {
            fd = Syscalls.open(path, flags, mode);
        } catch (SyscallException e) {
            if ((flags & OsConstants.O_CREAT) != 0) {
                e.maybeThrowInvalidFileNameException(path, null);
            }
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
        requireLinuxPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        return newFileChannel(file, options, attributes);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        requireLinuxPath(directory);
        Objects.requireNonNull(filter);
        String path = directory.toString();
        long dir;
        try {
            dir = Syscalls.opendir(path);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path);
        }
        return new LinuxDirectoryStream(directory, dir, filter);
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        requireLinuxPath(directory);
        Objects.requireNonNull(attributes);
        String path = directory.toString();
        int mode = PosixFileMode.toInt(PosixFileMode.fromAttributes(attributes,
                PosixFileMode.DEFAULT_MODE_CREATE_DIRECTORY));
        try {
            Syscalls.mkdir(path, mode);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(path, null);
            throw e.toFileSystemException(path);
        }
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attrs) throws IOException {
        requireLinuxPath(link);
        requireLinuxPath(target);
        Objects.requireNonNull(attrs);
        if (attrs.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attrs));
        }
        String targetString = target.toString();
        String linkPath = link.toString();
        try {
            Syscalls.symlink(targetString, linkPath);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(linkPath, null);
            throw e.toFileSystemException(linkPath, targetString);
        }
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        requireLinuxPath(link);
        requireLinuxPath(existing);
        String oldPath = existing.toString();
        String newPath = link.toString();
        try {
            Syscalls.link(oldPath, newPath);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(newPath, null);
            throw e.toFileSystemException(newPath, oldPath);
        }
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
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
        requireLinuxPath(link);
        String path = link.toString();
        String target;
        try {
            target = Syscalls.readlink(path);
        } catch (SyscallException e) {
            if (e.getErrno() == OsConstants.EINVAL) {
                FileSystemException exception = new NotLinkException(path);
                exception.initCause(e);
                throw exception;
            }
            throw e.toFileSystemException(path);
        }
        return new StringPath(target);
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        requireLinuxPath(source);
        requireLinuxPath(target);
        Objects.requireNonNull(options);
        String sourceString = source.toString();
        String targetString = target.toString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMove.copy(sourceString, targetString, copyOptions);
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        requireLinuxPath(source);
        requireLinuxPath(target);
        Objects.requireNonNull(options);
        String sourceString = source.toString();
        String targetString = target.toString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMove.move(sourceString, targetString, copyOptions);
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        requireLinuxPath(path);
        Objects.requireNonNull(path2);
        if (Objects.equals(path, path2)) {
            return true;
        }
        if (!(path instanceof LinuxPath)) {
            return false;
        }
        String pathString = path.toString();
        String path2String = path2.toString();
        StructStat pathStat;
        try {
            pathStat = Syscalls.lstat(pathString);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathString);
        }
        StructStat path2Stat;
        try {
            path2Stat = Syscalls.lstat(path2String);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path2String);
        }
        return pathStat.st_dev == path2Stat.st_dev && pathStat.st_ino == path2Stat.st_ino;
    }

    @Override
    public boolean isHidden(@NonNull Path path) {
        requireLinuxPath(path);
        String fileName = path.getFileName().toString();
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX);
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) {
        requireLinuxPath(path);
        String pathString = path.toString();
        return new LinuxFileStore(pathString);
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        requireLinuxPath(path);
        Objects.requireNonNull(modes);
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

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireLinuxPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        //noinspection unchecked
        return (V) getFileAttributeView(path, options);
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        requireLinuxPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!type.isAssignableFrom(LinuxFileAttributes.class)) {
            throw new UnsupportedOperationException(type.toString());
        }
        //noinspection unchecked
        return (A) getFileAttributeView(path, options).readAttributes();
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(LinuxFileAttributeView.class);
    }

    private static LinuxFileAttributeView getFileAttributeView(@NonNull Path path,
                                                               @NonNull LinkOption... options) {
        String pathString = path.toString();
        boolean noFollowLinks = LinkOptions.hasNoFollowLinks(options);
        return new LinuxFileAttributeView(pathString, noFollowLinks);
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) {
        requireLinuxPath(path);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) {
        requireLinuxPath(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    private static void requireLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof LinuxPath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
