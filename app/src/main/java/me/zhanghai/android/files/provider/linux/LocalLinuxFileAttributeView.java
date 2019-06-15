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
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LocalLinuxFileAttributeView implements PosixFileAttributeView {

    private static final String NAME = LinuxFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", "posix", NAME)));

    @NonNull
    private final ByteString mPath;
    private final boolean mNoFollowLinks;

    LocalLinuxFileAttributeView(@NonNull ByteString path, boolean noFollowLinks) {
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
            throw e.toFileSystemException(mPath.toString());
        }
        PosixUser owner;
        try {
            owner = LinuxUserPrincipalLookupService.getUserById(stat.st_uid);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        PosixGroup group;
        try {
            group = LinuxUserPrincipalLookupService.getGroupById(stat.st_gid);
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        ByteString seLinuxContext;
        try {
            if (mNoFollowLinks) {
                seLinuxContext = Syscalls.lgetfilecon(mPath);
            } else {
                seLinuxContext = Syscalls.getfilecon(mPath);
            }
        } catch (SyscallException e) {
            // Filesystem may not support xattrs and SELinux calls may fail with EOPNOTSUPP.
            e.toFileSystemException(mPath.toString()).printStackTrace();
            seLinuxContext = null;
        }
        return new LinuxFileAttributes(stat, owner, group, seLinuxContext);
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
            throw e.toFileSystemException(mPath.toString());
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

    @Override
    public void setOwner(@NonNull PosixUser owner) throws IOException {
        Objects.requireNonNull(owner);
        int uid = owner.getId();
        try {
            if (mNoFollowLinks) {
                Syscalls.lchown(mPath, uid, -1);
            } else {
                Syscalls.chown(mPath, uid, -1);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
    }

    @Override
    public void setGroup(@NonNull PosixGroup group) throws IOException {
        Objects.requireNonNull(group);
        int gid = group.getId();
        try {
            if (mNoFollowLinks) {
                Syscalls.lchown(mPath, -1, gid);
            } else {
                Syscalls.chown(mPath, -1, gid);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
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
            throw e.toFileSystemException(mPath.toString());
        }
    }

    @Override
    public void setSeLinuxContext(@NonNull ByteString context) throws IOException {
        Objects.requireNonNull(context);
        try {
            if (mNoFollowLinks) {
                Syscalls.lsetfilecon(mPath, context);
            } else {
                Syscalls.setfilecon(mPath, context);
            }
        } catch (SyscallException e) {
            throw e.toFileSystemException(mPath.toString());
        }
    }

    @Override
    public void restoreSeLinuxContext() throws IOException {
        ByteString path;
        if (mNoFollowLinks) {
            path = mPath;
        } else {
            try {
                path = Syscalls.realpath(mPath);
            } catch (SyscallException e) {
                throw e.toFileSystemException(mPath.toString());
            }
        }
        try {
            Syscalls.selinux_android_restorecon(path, 0);
        } catch (SyscallException e) {
            throw e.toFileSystemException(path.toString());
        }
    }
}
