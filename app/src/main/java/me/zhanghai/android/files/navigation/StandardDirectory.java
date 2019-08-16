/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class StandardDirectory {

    @DrawableRes
    private final int mIconRes;

    @StringRes
    private final int mTitleRes;

    @Nullable
    private final String mTitle;

    @NonNull
    private final String mRelativePath;

    private final boolean mEnabled;

    public StandardDirectory(@DrawableRes int iconRes, @StringRes int titleRes,
                             @NonNull String relativePath, boolean enabled) {
        this(iconRes, titleRes, null, relativePath, enabled);
    }

    private StandardDirectory(@DrawableRes int iconRes, @StringRes int titleRes,
                              @Nullable String title, @NonNull String relativePath,
                              boolean enabled) {
        mIconRes = iconRes;
        mTitleRes = titleRes;
        mTitle = title;
        mRelativePath = relativePath;
        mEnabled = enabled;
    }

    @NonNull
    public String getId() {
        return mRelativePath;
    }

    @DrawableRes
    public int getIconRes() {
        return mIconRes;
    }

    @NonNull
    public String getTitle(@NonNull Context context) {
        if (!TextUtils.isEmpty(mTitle)) {
            return mTitle;
        }
        return context.getString(mTitleRes);
    }

    @NonNull
    public String getRelativePath() {
        return mRelativePath;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    @NonNull
    public StandardDirectory withSettings(@Nullable StandardDirectorySettings settings) {
        if (settings == null) {
            return this;
        }
        return new StandardDirectory(mIconRes, mTitleRes, settings.getTitle(), mRelativePath,
                settings.isEnabled());
    }

    @NonNull
    public StandardDirectorySettings toSettings() {
        return new StandardDirectorySettings(getId(), mTitle, mEnabled);
    }
}
