/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import me.zhanghai.android.files.util.ViewUtils;

public class OverlayToolbarActionMode extends ToolbarActionMode {

    public OverlayToolbarActionMode(@NonNull Toolbar toolbar) {
        super(toolbar);
    }

    @Override
    protected void show(@NonNull Toolbar toolbar, boolean animate) {
        if (animate) {
            ViewUtils.fadeIn(toolbar);
        } else {
            ViewUtils.setVisibleOrGone(toolbar, true);
        }
    }

    @Override
    protected void hide(@NonNull Toolbar toolbar, boolean animate) {
        if (animate) {
            ViewUtils.fadeOut(toolbar);
        } else {
            ViewUtils.setVisibleOrGone(toolbar, false);
        }
    }
}
