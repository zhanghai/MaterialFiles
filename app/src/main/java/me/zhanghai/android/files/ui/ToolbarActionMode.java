/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import me.zhanghai.android.files.util.ViewUtils;

public class ToolbarActionMode {

    private static final String STATE_PREFIX = ToolbarActionMode.class.getName() + ".state.";
    private static final String STATE_TITLE = STATE_PREFIX + "TITLE";
    private static final String STATE_SUBTITLE = STATE_PREFIX + "SUBTITLE";
    private static final String STATE_MENU_RES = STATE_PREFIX + "MENU_RES";
    private static final String STATE_ACTIVE = STATE_PREFIX + "ACTIVE";

    @NonNull
    private transient Toolbar mToolbar;
    @NonNull
    private transient Callback mCallback;

    @MenuRes
    private int mMenuRes;

    private boolean mActive;

    public ToolbarActionMode(@NonNull Toolbar toolbar) {
        mToolbar = toolbar;
        ViewUtils.setVisibleOrGone(mToolbar, false);
        mToolbar.setNavigationOnClickListener(view -> finish());
        mToolbar.setOnMenuItemClickListener(item -> mCallback.onToolbarActionModeItemClicked(this,
                item));
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

    public boolean isActive() {
        return mActive;
    }

    public void start(@NonNull Callback callback) {
        start(callback, true);
    }

    private void start(@NonNull Callback callback, boolean animate) {
        mCallback = callback;
        mActive = true;
        if (animate) {
            ViewUtils.fadeIn(mToolbar);
        } else {
            ViewUtils.setVisibleOrGone(mToolbar, true);
        }
        mCallback.onToolbarActionModeStarted(this);
    }

    public void finish() {
        Callback callback = mCallback;
        mCallback = null;
        mActive = false;
        ViewUtils.fadeOut(mToolbar);
        callback.onToolbarActionModeFinished(this);
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putCharSequence(STATE_TITLE, mToolbar.getTitle());
        outState.putCharSequence(STATE_SUBTITLE, mToolbar.getSubtitle());
        outState.putInt(STATE_MENU_RES, mMenuRes);
        outState.putBoolean(STATE_ACTIVE, mActive);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState,
                                     @NonNull Callback callback) {
        setTitle(savedInstanceState.getCharSequence(STATE_TITLE));
        setSubtitle(savedInstanceState.getCharSequence(STATE_SUBTITLE));
        setMenuResource(savedInstanceState.getInt(STATE_MENU_RES));
        if (savedInstanceState.getBoolean(STATE_ACTIVE)) {
            start(callback, false);
        }
    }

    public interface Callback {

        void onToolbarActionModeStarted(@NonNull ToolbarActionMode toolbarActionMode);

        boolean onToolbarActionModeItemClicked(@NonNull ToolbarActionMode toolbarActionMode,
                                               @NonNull MenuItem item);

        void onToolbarActionModeFinished(@NonNull ToolbarActionMode toolbarActionMode);
    }
}
