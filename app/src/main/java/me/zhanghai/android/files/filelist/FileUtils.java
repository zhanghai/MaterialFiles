/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileTypeNames;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.FileNameUtils;

public class FileUtils {

    private FileUtils() {}

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
    public static String getTypeName(@NonNull FileItem file, @NonNull Context context) {
        if (file.getAttributesNoFollowLinks().isSymbolicLink() && file.isSymbolicLinkBroken()) {
            return FileTypeNames.getBrokenSymbolicLinkTypeName(context);
        }
        String extension = FileNameUtils.getExtension(getName(file));
        return FileTypeNames.getTypeName(file.getMimeType(), extension, context);
    }

    public static boolean isListable(@NonNull FileItem file) {
        return file.getAttributes().isDirectory() || MimeTypes.isSupportedArchive(
                file.getMimeType());
    }

    @NonNull
    public static Path toListablePath(@NonNull FileItem file) {
        Path path = file.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path) && MimeTypes.isSupportedArchive(
                file.getMimeType())) {
            return ArchiveFileSystemProvider.getRootPathForArchiveFile(path);
        }
        return path;
    }
}
