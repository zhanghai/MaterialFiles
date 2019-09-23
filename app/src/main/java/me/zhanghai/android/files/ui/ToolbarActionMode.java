/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

public abstract class ToolbarActionMode {

    @NonNull
    private final ViewGroup mBar;
    @NonNull
    private final Toolbar mToolbar;

    @Nullable
    private Callback mCallback;

    @MenuRes
    private int mMenuRes;

    public ToolbarActionMode(@NonNull ViewGroup bar, @NonNull Toolbar toolbar) {
        mBar = bar;
        mToolbar = toolbar;
        mToolbar.setNavigationOnClickListener(view -> finish());
        mToolbar.setOnMenuItemClickListener(item -> {
            if (mCallback == null) {
                return false;
            }
            return mCallback.onToolbarActionModeItemClicked(this, item);
        });
    }

    public void setNavigationIcon(@DrawableRes int iconRes) {
        mToolbar.setNavigationIcon(iconRes);
    }

    public void setNavigationIcon(@Nullable Drawable icon) {
        mToolbar.setNavigationIcon(icon);
    }

    public void setTitle(@StringRes int titleRes) {
        mToolbar.setTitle(titleRes);
    }

    public void setTitle(@Nullable CharSequence title) {
        mToolbar.setTitle(title);
    }

    public void setSubtitle(@StringRes int subtitleRes) {
        mToolbar.setSubtitle(subtitleRes);
    }

    public void setSubtitle(@Nullable CharSequence subtitle) {
        mToolbar.setSubtitle(subtitle);
    }

    public void setMenuResource(@MenuRes int menuRes) {
        if (mMenuRes == menuRes) {
            return;
        }
        mMenuRes = menuRes;
        Menu menu = mToolbar.getMenu();
        menu.clear();
        if (mMenuRes != 0) {
            mToolbar.inflateMenu(mMenuRes);
        }
    }

    @NonNull
    public Menu getMenu() {
        return mToolbar.getMenu();
    }

    public boolean isActive() {
        return mCallback != null;
    }

    public void start(@NonNull Callback callback) {
        start(callback, true);
    }

    public void start(@NonNull Callback callback, boolean animate) {
        mCallback = callback;
        show(mBar, animate);
        mCallback.onToolbarActionModeStarted(this);
    }

    protected abstract void show(@NonNull ViewGroup bar, boolean animate);

    public void finish() {
        finish(true);
    }

    public void finish(boolean animate) {
        if (mCallback == null) {
            return;
        }
        Callback callback = mCallback;
        mCallback = null;
        mToolbar.getMenu().close();
        hide(mBar, animate);
        callback.onToolbarActionModeFinished(this);
    }

    protected abstract void hide(@NonNull ViewGroup bar, boolean animate);

    public interface Callback {

        void onToolbarActionModeStarted(@NonNull ToolbarActionMode toolbarActionMode);

        boolean onToolbarActionModeItemClicked(@NonNull ToolbarActionMode toolbarActionMode,
                                               @NonNull MenuItem item);

        void onToolbarActionModeFinished(@NonNull ToolbarActionMode toolbarActionMode);
    }
}
