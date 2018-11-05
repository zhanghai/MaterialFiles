/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class ThemedSpeedDialView extends SpeedDialView {

    private boolean mSavingInstanceState;

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

    // HACK: Don't save the action items so that they don't overwrite our new themed items when
    // restoring saved state.
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        mSavingInstanceState = true;
        Parcelable savedState = super.onSaveInstanceState();
        mSavingInstanceState = false;
        return savedState;
    }

    @NonNull
    @Override
    public ArrayList<SpeedDialActionItem> getActionItems() {
        if (mSavingInstanceState) {
            return new ArrayList<>();
        }
        return super.getActionItems();
    }
}
