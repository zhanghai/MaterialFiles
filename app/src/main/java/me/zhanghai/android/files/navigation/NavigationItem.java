/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.storage.StorageVolume;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import java8.nio.file.Path;

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

    public boolean onLongClick(@NonNull Listener listener) {
        return false;
    }

    interface Listener {

        @NonNull
        Path getCurrentPath();

        void navigateTo(@NonNull Path path);

        void navigateToRoot(@NonNull Path path);

        void onAddDocumentTree();

        void onRemoveDocumentTree(@NonNull Uri treeUri, @Nullable StorageVolume storageVolume);

        void onEditBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory);

        void closeNavigationDrawer();

        void startActivity(@NonNull Intent intent);
    }
}
