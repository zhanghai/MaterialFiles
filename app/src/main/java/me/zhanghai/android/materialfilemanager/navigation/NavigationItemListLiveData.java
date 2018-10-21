/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.arch.lifecycle.LiveData;
import android.support.annotation.Nullable;

import java.util.List;

public class NavigationItemListLiveData extends LiveData<List<NavigationItem>> {

    @Nullable
    private static NavigationItemListLiveData sInstance;

    public static NavigationItemListLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new NavigationItemListLiveData();
        }
        return sInstance;
    }

    public NavigationItemListLiveData() {
        // TODO: Call addSource() for changes (from another live data).
        loadValue();
    }

    private void loadValue() {
        List<NavigationItem> navigationItems = NavigationItems.getItems();
        setValue(navigationItems);
    }
}
