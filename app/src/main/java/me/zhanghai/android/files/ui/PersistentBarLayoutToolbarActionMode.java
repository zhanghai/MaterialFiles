/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class PersistentBarLayoutToolbarActionMode extends ToolbarActionMode {

    @NonNull
    private final PersistentBarLayout mPersistentBarLayout;

    public PersistentBarLayoutToolbarActionMode(@NonNull PersistentBarLayout persistentBarLayout,
                                                @NonNull ViewGroup bar, @NonNull Toolbar toolbar) {
        super(bar, toolbar);

        mPersistentBarLayout = persistentBarLayout;
    }

    @Override
    protected void show(@NonNull ViewGroup bar, boolean animate) {
        mPersistentBarLayout.showBar(bar, animate);
    }

    @Override
    protected void hide(@NonNull ViewGroup bar, boolean animate) {
        mPersistentBarLayout.hideBar(bar, animate);
    }
}
