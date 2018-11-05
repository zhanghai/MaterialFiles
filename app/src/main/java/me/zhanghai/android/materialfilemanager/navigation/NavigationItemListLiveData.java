/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

public class NavigationItemListLiveData extends LiveData<List<NavigationItem>> {

    @Nullable
    private static NavigationItemListLiveData sInstance;

    @NonNull
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
