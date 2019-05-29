/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcelable;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.PosixFilePermission;

public interface PosixFileAttributes extends java8.nio.file.attribute.PosixFileAttributes {

    @NonNull
    PosixFileType type();

    @Override
    default boolean isRegularFile() {
        return type() == PosixFileType.REGULAR_FILE;
    }

    @Override
    default boolean isDirectory() {
        return type() == PosixFileType.DIRECTORY;
    }

    @Override
    default boolean isSymbolicLink() {
        return type() == PosixFileType.SYMBOLIC_LINK;
    }

    @Override
    default boolean isOther() {
        return !isRegularFile() && !isDirectory() && !isSymbolicLink();
    }

    @NonNull
    @Override
    Parcelable fileKey();

    @Override
    @Nullable
    PosixUser owner();

    @Override
    @Nullable
    PosixGroup group();

    @Nullable
    Set<PosixFileModeBit> mode();

    @Nullable
    @Override
    default Set<PosixFilePermission> permissions() {
        Set<PosixFileModeBit> mode = mode();
        return mode != null ? PosixFileMode.toPermissions(mode) : null;
    }

    @Nullable
    ByteString seLinuxContext();
}
