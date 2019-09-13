/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.file.FileTypeNames;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.FileNameUtils;

public class FileUtils {

    private FileUtils() {}

    @NonNull
    public static FileItem createDummyFileItemForArchiveRoot(@NonNull FileItem file) {
        Path path = ArchiveFileSystemProvider.getRootPathForArchiveFile(file.getPath());
        BasicFileAttributes attributes = new DummyArchiveRootBasicFileAttributes();
        String mimeType = MimeTypes.DIRECTORY_MIME_TYPE;
        return new FileItem(path, attributes, null, null, false, mimeType);
    }

    @NonNull
    public static String getExtension(@NonNull FileItem file) {
        if (file.getAttributes().isDirectory()) {
            return "";
        }
        return FileNameUtils.getExtension(getName(file));
    }

    @NonNull
    public static String getName(@NonNull FileItem file) {
        return getName(file.getPath());
    }

    @NonNull
    public static String getName(@NonNull Path path) {
        Path namePath = path.getFileName();
        if (namePath != null) {
            return namePath.toString();
        }
        if (ArchiveFileSystemProvider.isArchivePath(path)) {
            return ArchiveFileSystemProvider.getArchiveFile(path).getFileName().toString();
        }
        return "/";
    }

    @NonNull
    public static String getPathString(@NonNull Path path) {
        return LinuxFileSystemProvider.isLinuxPath(path) ? path.toFile().getPath()
                : path.toUri().toString();
    }

    @NonNull
    public static String getTypeName(@NonNull FileItem file, @NonNull Context context) {
        if (file.getAttributesNoFollowLinks().isSymbolicLink() && file.isSymbolicLinkBroken()) {
            return FileTypeNames.getBrokenSymbolicLinkTypeName(context);
        }
        String extension = FileNameUtils.getExtension(getName(file));
        return FileTypeNames.getTypeName(file.getMimeType(), extension, context);
    }

    public static boolean isArchiveFile(@NonNull Path path, @NonNull String mimeType) {
        return LinuxFileSystemProvider.isLinuxPath(path) && MimeTypes.isSupportedArchive(mimeType);
    }

    public static boolean isArchiveFile(@NonNull FileItem file) {
        return isArchiveFile(file.getPath(), file.getMimeType());
    }

    public static boolean isListable(@NonNull FileItem file) {
        return file.getAttributes().isDirectory() || isArchiveFile(file);
    }

    @NonNull
    public static Path toListablePath(@NonNull FileItem file) {
        Path path = file.getPath();
        if (isArchiveFile(file)) {
            return ArchiveFileSystemProvider.getRootPathForArchiveFile(path);
        }
        return path;
    }

    // Dummy attributes only to be added to the selection set, which may be used to determine file
    // type when confirming deletion.
    private static class DummyArchiveRootBasicFileAttributes implements BasicFileAttributes {

        @NonNull
        @Override
        public FileTime lastModifiedTime() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public FileTime lastAccessTime() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public FileTime creationTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public Object fileKey() {
            throw new UnsupportedOperationException();
        }
    }
}
