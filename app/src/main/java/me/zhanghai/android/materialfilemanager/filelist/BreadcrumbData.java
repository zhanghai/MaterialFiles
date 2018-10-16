/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.compat.Function;

public class BreadcrumbData {

    @NonNull
    public final List<File> files;

    @NonNull
    public final List<Function<Context, String>> items;

    public final int selectedIndex;

    public BreadcrumbData(@NonNull List<File> files, @NonNull List<Function<Context, String>> items,
                          int selectedIndex) {
        this.files = files;
        this.items = items;
        this.selectedIndex = selectedIndex;
    }
}
