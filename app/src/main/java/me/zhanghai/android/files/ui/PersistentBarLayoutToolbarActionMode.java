/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class PersistentBarLayoutToolbarActionMode extends ToolbarActionMode {

    @NonNull
    private final PersistentBarLayout mPersistentBarLayout;

    public PersistentBarLayoutToolbarActionMode(@NonNull Toolbar toolbar,
                                                @NonNull PersistentBarLayout persistentBarLayout) {
        super(toolbar);

        mPersistentBarLayout = persistentBarLayout;
    }

    @Override
    protected void show(@NonNull Toolbar toolbar, boolean animate) {
        mPersistentBarLayout.showBar(toolbar, animate);
    }

    @Override
    protected void hide(@NonNull Toolbar toolbar, boolean animate) {
        mPersistentBarLayout.hideBar(toolbar, animate);
    }
}
