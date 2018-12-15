/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.UserPrincipalLookupService;
import java8.nio.file.attribute.UserPrincipalNotFoundException;
import me.zhanghai.android.files.provider.linux.syscall.StructGroup;
import me.zhanghai.android.files.provider.linux.syscall.StructPasswd;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class LinuxUserPrincipalLookupService extends UserPrincipalLookupService {

    private static class Instance {

        @NonNull
        public static final LinuxUserPrincipalLookupService INSTANCE =
                new LinuxUserPrincipalLookupService();
    }

    @NonNull
    static LinuxUserPrincipalLookupService getInstance() {
        return Instance.INSTANCE;
    }

    private LinuxUserPrincipalLookupService() {}

    @NonNull
    @Override
    public LinuxUser lookupPrincipalByName(@NonNull String name) throws IOException {
        Objects.requireNonNull(name);
        StructPasswd passwd;
        try {
            passwd = Syscalls.getpwnam(name);
        } catch (SyscallException e) {
            throw e.toFileSystemException(null);
        }
        if (passwd == null) {
            throw new UserPrincipalNotFoundException(name);
        }
        return new LinuxUser(passwd.pw_uid, passwd.pw_name);
    }

    @NonNull
    public LinuxUser lookupPrincipalById(int id) throws IOException {
        try {
            return getUserById(id);
        } catch (SyscallException e) {
            throw e.toFileSystemException(null);
        }
    }

    @NonNull
    @Override
    public LinuxGroup lookupPrincipalByGroupName(@NonNull String group) throws IOException {
        Objects.requireNonNull(group);
        StructGroup groupStruct;
        try {
            groupStruct = Syscalls.getgrnam(group);
        } catch (SyscallException e) {
            throw e.toFileSystemException(null);
        }
        if (groupStruct == null) {
            throw new UserPrincipalNotFoundException(group);
        }
        return new LinuxGroup(groupStruct.gr_gid, groupStruct.gr_name);
    }

    @NonNull
    public LinuxGroup lookupPrincipalByGroupId(int groupId) throws IOException {
        try {
            return getGroupById(groupId);
        } catch (SyscallException e) {
            throw e.toFileSystemException(null);
        }
    }

    @NonNull
    static LinuxUser getUserById(int uid) throws SyscallException {
        StructPasswd passwd = Syscalls.getpwuid(uid);
        return new LinuxUser(uid, passwd != null ? passwd.pw_name : null);
    }

    @NonNull
    static LinuxGroup getGroupById(int gid) throws SyscallException {
        StructGroup group = Syscalls.getgrgid(gid);
        return new LinuxGroup(gid, group != null ? group.gr_name : null);
    }
}
