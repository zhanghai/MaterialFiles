/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import me.zhanghai.android.fastscroll.PopupStyles;
import me.zhanghai.android.files.settings.Settings;

public class ThemedFastScroller {

    private ThemedFastScroller() {}

    @NonNull
    public static FastScroller create(@NonNull ViewGroup view) {
        FastScrollerBuilder fastScrollerBuilder = new FastScrollerBuilder(view);
        if (Settings.MATERIAL_DESIGN_2.getValue()) {
            fastScrollerBuilder.useMd2Style();
        } else {
            fastScrollerBuilder.setPopupStyle(popupText -> {
                PopupStyles.DEFAULT.accept(popupText);
                popupText.setTextColor(Color.WHITE);
            });
        }
        return fastScrollerBuilder.build();
    }
}
