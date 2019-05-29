/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.GroupPrincipal;
import java8.nio.file.attribute.PosixFilePermission;
import java8.nio.file.attribute.UserPrincipal;

public interface PosixFileAttributeView extends java8.nio.file.attribute.PosixFileAttributeView {

    @NonNull
    @Override
    PosixFileAttributes readAttributes() throws IOException;

    @Nullable
    @Override
    default PosixUser getOwner() throws IOException {
        return readAttributes().owner();
    }

    @Override
    default void setOwner(@NonNull UserPrincipal owner) throws IOException {
        Objects.requireNonNull(owner);
        if (!(owner instanceof PosixUser)) {
            throw new UnsupportedOperationException(owner.toString());
        }
        PosixUser posixOwner = (PosixUser) owner;
        setOwner(posixOwner);
    }

    void setOwner(@NonNull PosixUser owner) throws IOException;

    @Override
    default void setGroup(@NonNull GroupPrincipal group) throws IOException {
        Objects.requireNonNull(group);
        if (!(group instanceof PosixGroup)) {
            throw new UnsupportedOperationException(group.toString());
        }
        PosixGroup posixGroup = (PosixGroup) group;
        setGroup(posixGroup);
    }

    void setGroup(@NonNull PosixGroup group) throws IOException;

    @Override
    default void setPermissions(@NonNull Set<PosixFilePermission> permissions) throws IOException {
        Objects.requireNonNull(permissions);
        setMode(PosixFileMode.fromPermissions(permissions));
    }

    void setMode(@NonNull Set<PosixFileModeBit> mode) throws IOException;

    void setSeLinuxContext(@NonNull ByteString context) throws IOException;

    void restoreSeLinuxContext() throws IOException;
}
