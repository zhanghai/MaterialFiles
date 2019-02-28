/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

// We cannot reference disabled text color in XML resource, so we have to do this in Java.
public class NavigationItemColor {

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final int[] DISABLED_STATE_SET = { -android.R.attr.state_enabled };
    private static final int[] EMPTY_STATE_SET = {};

    private NavigationItemColor() {}

    @NonNull
    public static ColorStateList create(@NonNull ColorStateList color, @NonNull Context context) {
        // The primary color doesn't have enough contrast against the window background color in a
        // dark theme.
        int checkedColorAttr = ViewUtils.isLightTheme(context) ? R.attr.colorPrimary
                : android.R.attr.textColorPrimary;
        int checkedColor = ViewUtils.getColorFromAttrRes(checkedColorAttr, 0, context);
        int defaultColor = color.getDefaultColor();
        int disabledColor = color.getColorForState(DISABLED_STATE_SET, defaultColor);
        return new ColorStateList(new int[][] {
                DISABLED_STATE_SET,
                CHECKED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[] {
                disabledColor,
                checkedColor,
                defaultColor
        });
    }
}
