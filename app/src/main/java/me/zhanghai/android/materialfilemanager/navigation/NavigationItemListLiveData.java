/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.arch.lifecycle.LiveData;

import java.util.List;

public class NavigationItemListLiveData extends LiveData<List<NavigationItem>> {

    private static NavigationItemListLiveData sInstance;

    public static NavigationItemListLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new NavigationItemListLiveData();
        }
        return sInstance;
    }

    public NavigationItemListLiveData() {
        // TODO: Register change listener, call loadData() if hasActiveObservers() or setValue(null)
        // otherwise.
    }

    @Override
    protected void onActive() {
        if (getValue() == null) {
            loadData();
        }
    }

    private void loadData() {
        setValue(NavigationItems.getItems());
    }
}
