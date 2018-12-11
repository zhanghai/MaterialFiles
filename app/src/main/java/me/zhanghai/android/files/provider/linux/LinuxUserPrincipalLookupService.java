/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.UserPrincipalLookupService;

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
        return null;
    }

    @NonNull
    public LinuxUser lookupPrincipalById(int id) throws IOException {
        return null;
    }

    @NonNull
    @Override
    public LinuxGroup lookupPrincipalByGroupName(@NonNull String group) throws IOException {
        Objects.requireNonNull(group);
        return null;
    }

    @NonNull
    public LinuxGroup lookupPrincipalByGroupId(int groupId) throws IOException {
        return null;
    }
}
