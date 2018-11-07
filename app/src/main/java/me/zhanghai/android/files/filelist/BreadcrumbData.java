/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.functional.compat.Function;

public class BreadcrumbData {

    @NonNull
    public final List<File> files;

    @NonNull
    public final List<Function<Context, String>> names;

    public final int selectedIndex;

    public BreadcrumbData(@NonNull List<File> files, @NonNull List<Function<Context, String>> names,
                          int selectedIndex) {
        this.files = files;
        this.names = names;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BreadcrumbData that = (BreadcrumbData) object;
        return selectedIndex == that.selectedIndex
                && Objects.equals(files, that.files)
                && Objects.equals(names, that.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files, names, selectedIndex);
    }
}
