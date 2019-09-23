/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import me.zhanghai.android.files.util.ViewUtils;

public class OverlayToolbarActionMode extends ToolbarActionMode {

    public OverlayToolbarActionMode(@NonNull ViewGroup bar, @NonNull Toolbar toolbar) {
        super(bar, toolbar);

        ViewUtils.setVisibleOrGone(bar, false);
    }

    public OverlayToolbarActionMode(@NonNull Toolbar toolbar) {
        this(toolbar, toolbar);
    }

    @Override
    protected void show(@NonNull ViewGroup bar, boolean animate) {
        if (animate) {
            ViewUtils.fadeIn(bar);
        } else {
            ViewUtils.setVisibleOrGone(bar, true);
        }
    }

    @Override
    protected void hide(@NonNull ViewGroup bar, boolean animate) {
        if (animate) {
            ViewUtils.fadeOut(bar);
        } else {
            ViewUtils.setVisibleOrGone(bar, false);
        }
    }
}
