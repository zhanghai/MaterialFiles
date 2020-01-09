/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
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
import java9.util.function.Consumer;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringPath;
import me.zhanghai.android.files.provider.common.ByteStringUriUtils;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.common.LinkOptions;
import me.zhanghai.android.files.provider.common.MoreFileChannels;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.provider.common.PathObservableProvider;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.Searchable;
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable;
import me.zhanghai.android.files.provider.common.WatchServicePathObservable;
import me.zhanghai.android.files.provider.linux.mediastore.MediaStore;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LocalLinuxFileSystemProvider extends FileSystemProvider
        implements PathObservableProvider, Searchable {

    static final String SCHEME = "file";

    private static final ByteString HIDDEN_FILE_NAME_PREFIX = ByteString.fromString(".");

    @NonNull
    private final LinuxFileSystemProvider mProvider;

    @NonNull
    private final LinuxFileSystem mFileSystem;

    LocalLinuxFileSystemProvider(@NonNull LinuxFileSystemProvider provider) {
        mProvider = provider;

        mFileSystem = new LinuxFileSystem(mProvider);
    }

    public static boolean isLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof LinuxPath;
    }

    @NonNull
    LinuxFileSystem getFileSystem() {
        return mFileSystem;
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
        ByteString path = ByteStringUriUtils.getDecodedPath(uri);
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
        LinuxPath linuxFile = requireLinuxPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        ByteString fileBytes = linuxFile.toByteString();
        OpenOptions openOptions = OpenOptions.fromSet(options);
        int flags = LinuxOpenOptions.toFlags(openOptions);
        int mode = PosixFileMode.toInt(PosixFileMode.fromAttributes(attributes,
                PosixFileMode.DEFAULT_MODE_CREATE_FILE));
        FileDescriptor fd;
        try {
            fd = Syscalls.open(fileBytes, flags, mode);
        } catch (SyscallException e) {
            if ((flags & OsConstants.O_CREAT) != 0) {
                e.maybeThrowInvalidFileNameException(fileBytes.toString());
            }
            throw e.toFileSystemException(fileBytes.toString());
        }
        FileChannel fileChannel = MoreFileChannels.open(fd, flags);
        if (openOptions.hasDeleteOnClose()) {
            try {
                Syscalls.remove(fileBytes);
            } catch (SyscallException e) {
                e.printStackTrace();
            }
        }
        File javaFile = linuxFile.toFile();
        MediaStore.scan(javaFile);
        return MediaStore.newScanOnCloseFileChannel(fileChannel, javaFile);
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
        LinuxPath linuxDirectory = requireLinuxPath(directory);
        Objects.requireNonNull(filter);
        ByteString directoryBytes = linuxDirectory.toByteString();
        long dir;
        try {
            dir = Syscalls.opendir(directoryBytes);
        } catch (SyscallException e) {
            throw e.toFileSystemException(directoryBytes.toString());
        }
        return new LinuxDirectoryStream(linuxDirectory, dir, filter);
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        LinuxPath linuxDirectory = requireLinuxPath(directory);
        Objects.requireNonNull(attributes);
        ByteString directoryBytes = linuxDirectory.toByteString();
        int mode = PosixFileMode.toInt(PosixFileMode.fromAttributes(attributes,
                PosixFileMode.DEFAULT_MODE_CREATE_DIRECTORY));
        try {
            Syscalls.mkdir(directoryBytes, mode);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(directoryBytes.toString());
            throw e.toFileSystemException(directoryBytes.toString());
        }
        MediaStore.scan(linuxDirectory.toFile());
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) throws IOException {
        LinuxPath linuxLink = requireLinuxPath(link);
        ByteString targetBytes = requireLinuxOrByteStringPath(target);
        Objects.requireNonNull(attributes);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        ByteString linkBytes = linuxLink.toByteString();
        try {
            Syscalls.symlink(targetBytes, linkBytes);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(linkBytes.toString());
            throw e.toFileSystemException(linkBytes.toString(), targetBytes.toString());
        }
        MediaStore.scan(linuxLink.toFile());
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) throws IOException {
        LinuxPath linuxLink = requireLinuxPath(link);
        LinuxPath linuxExisting = requireLinuxPath(existing);
        ByteString oldPathBytes = linuxExisting.toByteString();
        ByteString newPathBytes = linuxLink.toByteString();
        try {
            Syscalls.link(oldPathBytes, newPathBytes);
        } catch (SyscallException e) {
            e.maybeThrowInvalidFileNameException(newPathBytes.toString());
            throw e.toFileSystemException(newPathBytes.toString(), oldPathBytes.toString());
        }
        MediaStore.scan(linuxLink.toFile());
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        LinuxPath linuxPath = requireLinuxPath(path);
        ByteString pathBytes = linuxPath.toByteString();
        try {
            Syscalls.remove(pathBytes);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathBytes.toString());
        }
        MediaStore.scan(linuxPath.toFile());
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        LinuxPath linuxLink = requireLinuxPath(link);
        ByteString linkBytes = linuxLink.toByteString();
        ByteString targetBytes;
        try {
            targetBytes = Syscalls.readlink(linkBytes);
        } catch (SyscallException e) {
            if (e.getErrno() == OsConstants.EINVAL) {
                FileSystemException exception = new NotLinkException(linkBytes.toString());
                exception.initCause(e);
                throw exception;
            }
            throw e.toFileSystemException(linkBytes.toString());
        }
        return new ByteStringPath(targetBytes);
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        LinuxPath linuxSource = requireLinuxPath(source);
        LinuxPath linuxTarget = requireLinuxPath(target);
        Objects.requireNonNull(options);
        ByteString sourceBytes = linuxSource.toByteString();
        ByteString targetBytes = linuxTarget.toByteString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMove.copy(sourceBytes, targetBytes, copyOptions);
        MediaStore.scan(linuxTarget.toFile());
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        LinuxPath linuxSource = requireLinuxPath(source);
        LinuxPath linuxTarget = requireLinuxPath(target);
        Objects.requireNonNull(options);
        ByteString sourceBytes = linuxSource.toByteString();
        ByteString targetBytes = linuxTarget.toByteString();
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        LinuxCopyMove.move(sourceBytes, targetBytes, copyOptions);
        MediaStore.scan(linuxSource.toFile());
        MediaStore.scan(linuxTarget.toFile());
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) throws IOException {
        LinuxPath linuxPath = requireLinuxPath(path);
        Objects.requireNonNull(path2);
        if (Objects.equals(linuxPath, path2)) {
            return true;
        }
        if (!(path2 instanceof LinuxPath)) {
            return false;
        }
        LinuxPath linuxPath2 = requireLinuxPath(path2);
        ByteString pathBytes = linuxPath.toByteString();
        ByteString path2Bytes = linuxPath2.toByteString();
        StructStat pathStat;
        try {
            pathStat = Syscalls.lstat(pathBytes);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathBytes.toString());
        }
        StructStat path2Stat;
        try {
            path2Stat = Syscalls.lstat(path2Bytes);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path2Bytes.toString());
        }
        return pathStat.st_dev == path2Stat.st_dev && pathStat.st_ino == path2Stat.st_ino;
    }

    @Override
    public boolean isHidden(@NonNull Path path) {
        LinuxPath linuxPath = requireLinuxPath(path);
        LinuxPath fileName = linuxPath.getFileName();
        if (fileName == null) {
            return false;
        }
        ByteString fileNameBytes = fileName.toByteString();
        return fileNameBytes.startsWith(HIDDEN_FILE_NAME_PREFIX);
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) throws IOException {
        LinuxPath linuxPath = requireLinuxPath(path);
        return new LinuxFileStore(linuxPath);
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        LinuxPath linuxPath = requireLinuxPath(path);
        Objects.requireNonNull(modes);
        ByteString pathBytes = linuxPath.toByteString();
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
            accessible = Syscalls.access(pathBytes, mode);
        } catch (SyscallException e) {
            throw e.toFileSystemException(pathBytes.toString());
        }
        if (!accessible) {
            throw new AccessDeniedException(pathBytes.toString());
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
        LinuxPath linuxPath = requireLinuxPath(path);
        boolean noFollowLinks = LinkOptions.hasNoFollowLinks(options);
        return new LinuxFileAttributeView(linuxPath, noFollowLinks);
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

    @NonNull
    @Override
    public PathObservable observePath(@NonNull Path path, long intervalMillis) throws IOException {
        requireLinuxPath(path);
        return new WatchServicePathObservable(path, intervalMillis);
    }

    @Override
    public void search(@NonNull Path directory, @NonNull String query,
                       @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        requireLinuxPath(directory);
        Objects.requireNonNull(query);
        Objects.requireNonNull(listener);
        WalkFileTreeSearchable.search(directory, query, listener, intervalMillis);
    }

    @NonNull
    private static LinuxPath requireLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof LinuxPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (LinuxPath) path;
    }

    private static ByteString requireLinuxOrByteStringPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (path instanceof LinuxPath) {
            return ((LinuxPath) path).toByteString();
        } else if (path instanceof ByteStringPath) {
            return ((ByteStringPath) path).toByteString();
        } else {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
