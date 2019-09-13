/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.content.pm.ApplicationInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PrincipalItem {

    public final int id;
    @Nullable
    public final String name;
    @NonNull
    public final List<ApplicationInfo> applicationInfos;
    @NonNull
    public final List<String> applicationLabels;

    public PrincipalItem(int id, @Nullable String name,
                         @NonNull List<ApplicationInfo> applicationInfos,
                         @NonNull List<String> applicationLabels) {
        this.id = id;
        this.name = name;
        this.applicationInfos = applicationInfos;
        this.applicationLabels = applicationLabels;
    }
}
