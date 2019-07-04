/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FixQueryChangeSearchView extends FixLayoutSearchView {

    private boolean mShouldIgnoreQueryChange;

    public FixQueryChangeSearchView(@NonNull Context context) {
        super(context);
    }

    public FixQueryChangeSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixQueryChangeSearchView(@NonNull Context context, @Nullable AttributeSet attrs,
                                    int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean shouldIgnoreQueryChange() {
        return mShouldIgnoreQueryChange;
    }

    @Override
    public void setIconified(boolean iconify) {
        mShouldIgnoreQueryChange = true;
        super.setIconified(iconify);
        mShouldIgnoreQueryChange = false;
    }

    @Override
    public void onActionViewCollapsed() {
        mShouldIgnoreQueryChange = true;
        super.onActionViewCollapsed();
        mShouldIgnoreQueryChange = false;
    }

    @Override
    public void onActionViewExpanded() {
        mShouldIgnoreQueryChange = true;
        super.onActionViewExpanded();
        mShouldIgnoreQueryChange = false;
    }
}
