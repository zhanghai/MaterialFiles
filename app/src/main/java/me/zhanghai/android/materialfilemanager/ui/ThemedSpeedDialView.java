/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

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

    /**
     * Inflate a menu resource into this SpeedDialView. Any existing Action item will be removed.
     * <p class="note">Using the Menu resource it is possible to specify only the ID, the icon and the label of the
     * Action item. No color customization is available.</p>
     *
     * @param menuRes Menu resource to inflate
     */
    public void inflate(@MenuRes int menuRes) {
        clearActionItems();
        Context context = getContext();
        PopupMenu popupMenu = new PopupMenu(context, new View(context));
        popupMenu.inflate(menuRes);
        Menu menu = popupMenu.getMenu();
        int floatingBackgroundColor = ViewUtils.getColorFromAttrRes(R.attr.colorBackgroundFloating,
                0, context);
        int secondaryTextColor = ViewUtils.getColorFromAttrRes(android.R.attr.textColorSecondary, 0,
                context);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpeedDialActionItem actionItem = new SpeedDialActionItem.Builder(menuItem.getItemId(),
                    menuItem.getIcon())
                    .setLabel(menuItem.getTitle() != null ? menuItem.getTitle().toString() : null)
                    .setLabelBackgroundColor(floatingBackgroundColor)
                    .setLabelColor(secondaryTextColor)
                    .create();
            addActionItem(actionItem);
        }
    }
}
