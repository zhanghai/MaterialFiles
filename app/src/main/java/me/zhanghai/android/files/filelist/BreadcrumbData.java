/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java9.util.function.Function;

public class BreadcrumbData {

    @NonNull
    public final List<Path> paths;

    @NonNull
    public final List<Function<Context, String>> names;

    public final int selectedIndex;

    public BreadcrumbData(@NonNull List<Path> paths, @NonNull List<Function<Context, String>> names,
                          int selectedIndex) {
        this.paths = paths;
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
                && Objects.equals(paths, that.paths)
                && Objects.equals(names, that.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths, names, selectedIndex);
    }
}
