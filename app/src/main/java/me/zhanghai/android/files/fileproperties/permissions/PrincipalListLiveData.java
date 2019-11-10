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
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.compat.MapCompat;
import me.zhanghai.android.files.util.ObjectUtils;
import me.zhanghai.java.functional.Functional;

abstract class PrincipalListLiveData extends MutableLiveData<PrincipalListData> {

    protected static final int AID_USER_OFFSET = 100000;
    protected static final int AID_APP_START = 10000;
    protected static final int AID_CACHE_GID_START = 20000;
    protected static final int AID_CACHE_GID_END = 29999;
    protected static final int AID_SHARED_GID_START = 50000;
    protected static final int AID_SHARED_GID_END = 59999;
    protected static final int AID_ISOLATED_START = 99000;

    public PrincipalListLiveData() {
        loadValue();
    }

    private void loadValue() {
        setValue(PrincipalListData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            PrincipalListData value;
            try {
                List<PrincipalItem> principals = getAndroidPrincipals();
                Set<Integer> androidIds = Functional.map(principals, principal -> principal.id,
                        new HashSet<>());
                PackageManager packageManager = AppProvider.requireContext().getPackageManager();
                List<ApplicationInfo> installedApplicationInfos =
                        packageManager.getInstalledApplications(0);
                Map<Integer, List<ApplicationInfo>> uidApplicationInfoMap = new HashMap<>();
                for (ApplicationInfo applicationInfo : installedApplicationInfos) {
                    int uid = applicationInfo.uid;
                    if (androidIds.contains(uid)) {
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
                    String name = getAppPrincipalName(uid);
                    List<String> applicationLabels = Functional.map(applicationInfos,
                            applicationInfo -> ObjectUtils.toStringOrNull(
                                    packageManager.getApplicationLabel(applicationInfo)));
                    PrincipalItem principal = new PrincipalItem(uid, name, applicationInfos,
                            applicationLabels);
                    principals.add(principal);
                }
                Collections.sort(principals, Comparators.comparing(principalItem ->
                        principalItem.id));
                value = PrincipalListData.ofSuccess(principals);
            } catch (Exception e) {
                value = PrincipalListData.ofError(e);
            }
            postValue(value);
        });
    }

    @NonNull
    protected abstract List<PrincipalItem> getAndroidPrincipals() throws Exception;

    @NonNull
    protected abstract String getAppPrincipalName(int uid);
}
