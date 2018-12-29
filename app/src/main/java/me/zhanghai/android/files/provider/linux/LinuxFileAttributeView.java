/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import org.threeten.bp.Instant;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;
import java8.nio.file.attribute.GroupPrincipal;
import java8.nio.file.attribute.PosixFileAttributeView;
import java8.nio.file.attribute.PosixFilePermission;
import java8.nio.file.attribute.UserPrincipal;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LinuxFileAttributeView implements PosixFileAttributeView {

    private static final String NAME = "linux";

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", "posix", NAME)));

    @NonNull
    private final String mPath;
    private final boolean mNoFollowLinks;

    LinuxFileAttributeView(@NonNull String path, boolean noFollowLinks) {
        mPath = path;
        mNoFollowLinks = noFollowLinks;
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public LinuxFileAttributes readAttributes() throws IOException {
        StructStat stat;
        try {
            if (mNoFollowLinks) {
                stat = Syscalls.lstat(mPath);
            } else {
                stat = Syscalls.stat(mPath);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
        PosixUser owner;
        try {
            owner = LinuxUserPrincipalLookupService.getUserById(stat.st_uid);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
        PosixGroup group;
        try {
            group = LinuxUserPrincipalLookupService.getGroupById(stat.st_gid);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
        return new LinuxFileAttributes(stat, owner, group);
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) throws IOException {
        if (createTime != null) {
            throw new UnsupportedOperationException("createTime");
        }
        if (lastAccessTime == null && lastModifiedTime == null) {
            return;
        }
        StructTimespec[] times = {
                fileTimeToTimespec(lastAccessTime),
                fileTimeToTimespec(lastModifiedTime)
        };
        try {
            if (mNoFollowLinks) {
                Syscalls.lutimens(mPath, times);
            } else {
                Syscalls.utimens(mPath, times);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
    }

    @NonNull
    private static StructTimespec fileTimeToTimespec(@Nullable FileTime fileTime) {
        if (fileTime == null) {
            return new StructTimespec(0, Constants.UTIME_OMIT);
        }
        Instant instant = fileTime.toInstant();
        return new StructTimespec(instant.getEpochSecond(), instant.getNano());
    }

    @NonNull
    @Override
    public UserPrincipal getOwner() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(@NonNull UserPrincipal owner) throws IOException {
        Objects.requireNonNull(owner);
        if (!(owner instanceof PosixUser)) {
            throw new UnsupportedOperationException(owner.toString());
        }
        PosixUser posixOwner = (PosixUser) owner;
        int uid = posixOwner.getId();
        try {
            if (mNoFollowLinks) {
                Syscalls.lchown(mPath, uid, -1);
            } else {
                Syscalls.chown(mPath, uid, -1);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
    }

    @Override
    public void setGroup(@NonNull GroupPrincipal group) throws IOException {
        Objects.requireNonNull(group);
        if (!(group instanceof PosixGroup)) {
            throw new UnsupportedOperationException(group.toString());
        }
        PosixGroup posixGroup = (PosixGroup) group;
        int gid = posixGroup.getId();
        try {
            if (mNoFollowLinks) {
                Syscalls.lchown(mPath, -1, gid);
            } else {
                Syscalls.chown(mPath, -1, gid);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
    }

    @Override
    public void setPermissions(@NonNull Set<PosixFilePermission> permissions) throws IOException {
        Objects.requireNonNull(permissions);
        setMode(PosixFileMode.fromPermissions(permissions));
    }

    public void setMode(@NonNull Set<PosixFileModeBit> mode) throws IOException {
        Objects.requireNonNull(mode);
        if (mNoFollowLinks) {
            throw new UnsupportedOperationException("Cannot set mode for symbolic links");
        }
        int modeInt = PosixFileMode.toInt(mode);
        try {
            Syscalls.chmod(mPath, modeInt);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath);
        }
    }
}
