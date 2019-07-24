/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.spi.FileTypeDetector;
import me.zhanghai.android.files.file.MimeTypes;

public class AndroidFileTypeDetector extends FileTypeDetector {

    private AndroidFileTypeDetector() {}

    public static void install() {
        Files.installFileTypeDetector(new AndroidFileTypeDetector());
    }

    @NonNull
    @Override
    public String probeContentType(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        return getMimeType(path, attributes);
    }

    @NonNull
    public static String getMimeType(@NonNull Path path, @NonNull BasicFileAttributes attributes) {
        Objects.requireNonNull(path);
        PosixFileType posixFileType = PosixFileTypes.fromFileAttributes(attributes);
        String posixMimeType = MimeTypes.getPosixMimeType(posixFileType);
        if (posixMimeType != null) {
            return posixMimeType;
        }
        if (attributes.isDirectory()) {
            return MimeTypes.DIRECTORY_MIME_TYPE;
        }
        if (attributes instanceof ContentProviderFileAttributes) {
            ContentProviderFileAttributes contentProviderAttributes =
                    (ContentProviderFileAttributes) attributes;
            String contentProviderMimeType = contentProviderAttributes.mimeType();
            if (contentProviderMimeType != null) {
                return contentProviderMimeType;
            }
        }
        return MimeTypes.getMimeType(path.toString());
    }
}
