/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.linux.syscall.StructGroup;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.util.ObjectUtils;

class GroupListLiveData extends PrincipalListLiveData {

    @NonNull
    @Override
    protected List<PrincipalItem> getAndroidPrincipals() throws Exception {
        List<PrincipalItem> groups = new ArrayList<>();
        Syscalls.setgrent();
        try {
            StructGroup structGroup;
            while ((structGroup = Syscalls.getgrent()) != null) {
                int id = structGroup.gr_gid;
                String name = ObjectUtils.toStringOrNull(structGroup.gr_name);
                PrincipalItem group = new PrincipalItem(id, name, Collections.emptyList(),
                        Collections.emptyList());
                groups.add(group);
            }
        } finally {
            Syscalls.endgrent();
        }
        return groups;
    }

    /*
     * @see https://android.googlesource.com/platform/bionic/+/android10-release/libc/bionic/grp_grd.cpp
     *      print_app_name_from_gid()
     */
    @NonNull
    @Override
    protected String getAppPrincipalName(int uid) {
        int userId = uid / AID_USER_OFFSET;
        int appId = uid % AID_USER_OFFSET;
        if (appId > AID_ISOLATED_START) {
            return "u" + userId + "_i" + (appId - AID_ISOLATED_START);
        } else if (userId == 0 && (appId >= AID_SHARED_GID_START && appId <= AID_SHARED_GID_END)) {
            return "all_a" + (appId - AID_SHARED_GID_START);
        } else if (appId >= AID_CACHE_GID_START && appId <= AID_CACHE_GID_END) {
            return "u" + userId + "_a" + (appId - AID_CACHE_GID_START) + "_cache";
        } else {
            return "u" + userId + "_a" + (appId - AID_APP_START);
        }
    }
}
