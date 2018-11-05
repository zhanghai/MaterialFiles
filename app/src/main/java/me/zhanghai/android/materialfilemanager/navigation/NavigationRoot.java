/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.content.Context;

import androidx.annotation.NonNull;
import me.zhanghai.android.materialfilemanager.filesystem.File;

public interface NavigationRoot {

    @NonNull
    File getFile();

    @NonNull
    String getName(@NonNull Context context);
}
