/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import org.threeten.bp.Instant;

import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileTime;
import java8.nio.file.attribute.PosixFileAttributes;
import java8.nio.file.attribute.PosixFilePermission;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;

public class LinuxFileAttributes implements PosixFileAttributes {

    @NonNull
    private final StructStat mStat;
    @NonNull
    private final PosixUser mOwner;
    @NonNull
    private final PosixGroup mGroup;

    LinuxFileAttributes(@NonNull StructStat stat, @NonNull PosixUser owner,
                        @NonNull PosixGroup group) {
        mStat = stat;
        mOwner = owner;
        mGroup = group;
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.ofEpochSecond(mStat.st_mtim.tv_sec, mStat.st_mtim.tv_nsec));
    }

    @NonNull
    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(Instant.ofEpochSecond(mStat.st_atim.tv_sec, mStat.st_atim.tv_nsec));
    }

    @NonNull
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return OsConstants.S_ISREG(mStat.st_mode);
    }

    @Override
    public boolean isDirectory() {
        return OsConstants.S_ISDIR(mStat.st_mode);
    }

    @Override
    public boolean isSymbolicLink() {
        return OsConstants.S_ISLNK(mStat.st_mode);
    }

    @Override
    public boolean isOther() {
        return !isRegularFile() && !isDirectory() && !isSymbolicLink();
    }

    @Override
    public long size() {
        return mStat.st_size;
    }

    @NonNull
    @Override
    public Object fileKey() {
        return new LinuxFileKey(mStat.st_dev, mStat.st_ino);
    }

    @NonNull
    @Override
    public PosixUser owner() {
        return mOwner;
    }

    @NonNull
    @Override
    public PosixGroup group() {
        return mGroup;
    }

    @NonNull
    @Override
    public Set<PosixFilePermission> permissions() {
        return PosixFileMode.toPermissions(mode());
    }

    @NonNull
    public Set<PosixFileModeBit> mode() {
        return PosixFileMode.fromInt(mStat.st_mode);
    }
}
