/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import java9.util.Comparators;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.compat.MapCompat;
import me.zhanghai.android.files.provider.linux.syscall.StructPasswd;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.util.ObjectUtils;
import me.zhanghai.java.functional.Functional;

class UserListLiveData extends MutableLiveData<UserListData> {

    public UserListLiveData() {
        loadValue();
    }

    private void loadValue() {
        setValue(UserListData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            UserListData value;
            try {
                List<UserItem> users = new ArrayList<>();
                Set<Integer> androidUids = new HashSet<>();
                Syscalls.setpwent();
                try {
                    StructPasswd passwd;
                    while ((passwd = Syscalls.getpwent()) != null) {
                        int uid = passwd.pw_uid;
                        androidUids.add(uid);
                        String name = ObjectUtils.toStringOrNull(passwd.pw_name);
                        UserItem user = new UserItem(uid, name, Collections.emptyList(),
                                Collections.emptyList());
                        users.add(user);
                    }
                } finally {
                    Syscalls.endpwent();
                }
                PackageManager packageManager = AppApplication.getInstance().getPackageManager();
                List<ApplicationInfo> installedApplicationInfos =
                        packageManager.getInstalledApplications(0);
                Map<Integer, List<ApplicationInfo>> uidApplicationInfoMap = new HashMap<>();
                for (ApplicationInfo applicationInfo : installedApplicationInfos) {
                    int uid = applicationInfo.uid;
                    if (androidUids.contains(uid)) {
                        continue;
                    }
                    List<ApplicationInfo> applicationInfos = MapCompat.computeIfAbsent(
                            uidApplicationInfoMap, uid, _1 -> new ArrayList<>());
                    applicationInfos.add(applicationInfo);
                }
                for (Map.Entry<Integer, List<ApplicationInfo>> entry
                        : uidApplicationInfoMap.entrySet()) {
                    int uid = entry.getKey();
                    List<ApplicationInfo> applicationInfos = entry.getValue();
                    String name = getAppUidName(uid);
                    List<String> applicationLabels = Functional.map(applicationInfos,
                            applicationInfo -> ObjectUtils.toStringOrNull(
                                    packageManager.getApplicationLabel(applicationInfo)));
                    UserItem user = new UserItem(uid, name, applicationInfos, applicationLabels);
                    users.add(user);
                }
                Collections.sort(users, Comparators.comparing(userItem -> userItem.uid));
                value = UserListData.ofSuccess(users);
            } catch (Exception e) {
                value = UserListData.ofError(e);
            }
            postValue(value);
        });
    }

    /*
     * @see https://android.googlesource.com/platform/bionic/+/android10-release/libc/bionic/grp_pwd.cpp
     *      print_app_name_from_uid()
     */
    @NonNull
    private static String getAppUidName(int uid) {
        int userId = uid / 100000 /* UserHandle.PER_USER_RANGE */;
        int appId = uid % 100000 /* UserHandle.PER_USER_RANGE */;
        if (appId > 99000 /* Process.FIRST_ISOLATED_UID */) {
            return "u" + userId + "_i" + (appId - 99000 /* Process.FIRST_ISOLATED_UID */);
        } else {
            return "u" + userId + "_a" + (appId - 10000 /* Process.FIRST_APPLICATION_UID */);
        }
    }
}
