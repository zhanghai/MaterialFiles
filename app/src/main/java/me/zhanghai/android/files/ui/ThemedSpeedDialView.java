/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import androidx.annotation.Nullable;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class ThemedSpeedDialView extends SpeedDialView {

    public ThemedSpeedDialView(Context context) {
        super(context);
    }

    public ThemedSpeedDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedSpeedDialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addActionItem(SpeedDialActionItem actionItem, int position, boolean animate) {
        Context context = getContext();
        int labelColor = ViewUtils.getColorFromAttrRes(android.R.attr.textColorSecondary, 0,
                context);
        int labelBackgroundColor = ViewUtils.getColorFromAttrRes(R.attr.colorBackgroundFloating, 0,
                context);
        actionItem = new SpeedDialActionItem.Builder(actionItem.getId(),
                // Should not be a resource, pass null to fail fast.
                actionItem.getFabImageDrawable(null))
                .setLabel(actionItem.getLabel())
                .setFabImageTintColor(actionItem.getFabImageTintColor())
                .setFabBackgroundColor(actionItem.getFabBackgroundColor())
                .setLabelColor(labelColor)
                .setLabelBackgroundColor(labelBackgroundColor)
                .setLabelClickable(actionItem.isLabelClickable())
                .setTheme(actionItem.getTheme())
                .create();
        super.addActionItem(actionItem, position, animate);
    }
}
