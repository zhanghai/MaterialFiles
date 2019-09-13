/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.linux.syscall.StructPasswd;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.util.ObjectUtils;

class UserListLiveData extends PrincipalListLiveData {

    @NonNull
    @Override
    protected List<PrincipalItem> getAndroidPrincipals() throws Exception {
        List<PrincipalItem> users = new ArrayList<>();
        Syscalls.setpwent();
        try {
            StructPasswd passwd;
            while ((passwd = Syscalls.getpwent()) != null) {
                int id = passwd.pw_uid;
                String name = ObjectUtils.toStringOrNull(passwd.pw_name);
                PrincipalItem user = new PrincipalItem(id, name, Collections.emptyList(),
                        Collections.emptyList());
                users.add(user);
            }
        } finally {
            Syscalls.endpwent();
        }
        return users;
    }

    /*
     * @see https://android.googlesource.com/platform/bionic/+/android10-release/libc/bionic/grp_pwd.cpp
     *      print_app_name_from_uid()
     */
    @NonNull
    @Override
    protected String getAppPrincipalName(int uid) {
        int userId = uid / AID_USER_OFFSET;
        int appId = uid % AID_USER_OFFSET;
        if (appId > AID_ISOLATED_START) {
            return "u" + userId + "_i" + (appId - AID_ISOLATED_START);
        } else {
            return "u" + userId + "_a" + (appId - AID_APP_START);
        }
    }
}
