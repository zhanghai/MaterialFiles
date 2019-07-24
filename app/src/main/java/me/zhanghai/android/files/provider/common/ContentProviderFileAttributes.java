/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Objects;

import androidx.annotation.Nullable;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.file.MimeTypes;

public interface ContentProviderFileAttributes extends BasicFileAttributes {

    @Nullable
    String mimeType();

    @Override
    default boolean isRegularFile() {
        return !isDirectory();
    }

    @Override
    default boolean isDirectory() {
        return Objects.equals(mimeType(), MimeTypes.DIRECTORY_MIME_TYPE);
    }

    @Override
    default boolean isSymbolicLink() {
        return false;
    }

    @Override
    default boolean isOther() {
        return false;
    }
}
