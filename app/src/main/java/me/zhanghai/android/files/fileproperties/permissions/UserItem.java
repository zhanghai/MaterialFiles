/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.content.pm.ApplicationInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserItem {

    public final int uid;
    @Nullable
    public final String name;
    @NonNull
    public final List<ApplicationInfo> applicationInfos;
    @NonNull
    public final List<String> applicationLabels;

    public UserItem(int uid, @Nullable String name, @NonNull List<ApplicationInfo> applicationInfos,
                    @NonNull List<String> applicationLabels) {
        this.uid = uid;
        this.name = name;
        this.applicationInfos = applicationInfos;
        this.applicationLabels = applicationLabels;
    }
}
