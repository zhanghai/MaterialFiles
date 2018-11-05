/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import me.zhanghai.android.materialfilemanager.filesystem.File;

public abstract class NavigationItem {

    public abstract long getId();

    @NonNull
    public Drawable getIcon(@NonNull Context context) {
        return AppCompatResources.getDrawable(context, getIconRes());
    }

    @DrawableRes
    protected abstract int getIconRes();

    @NonNull
    public String getTitle(@NonNull Context context) {
        return context.getString(getTitleRes());
    }

    @StringRes
    protected abstract int getTitleRes();

    @Nullable
    public String getSubtitle(@NonNull Context context) {
        return null;
    }

    public boolean isChecked(@NonNull Listener listener) {
        return false;
    }

    public abstract void onClick(@NonNull Listener listener);

    interface Listener {

        @NonNull
        File getCurrentFile();

        void navigateToFile(@NonNull File file);

        void navigateToRoot(@NonNull File file);

        void onAddDocumentTree();

        void closeNavigationDrawer();

        void startActivity(@NonNull Intent intent);
    }
}
