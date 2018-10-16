/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.arch.lifecycle.MediatorLiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.filesystem.File;

public class NavigationRootMapLiveData extends MediatorLiveData<Map<File, NavigationRoot>> {

    private static NavigationRootMapLiveData sInstance;

    public static NavigationRootMapLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new NavigationRootMapLiveData();
        }
        return sInstance;
    }

    public NavigationRootMapLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
        addSource(NavigationItemListLiveData.getInstance(), navigationItems -> loadValue());
    }

    private void loadValue() {
        List<NavigationItem> navigationItems = NavigationItemListLiveData.getInstance().getValue();
        Map<File, NavigationRoot> fileItemMap = new HashMap<>();
        for (NavigationItem navigationItem : navigationItems) {
            if (navigationItem instanceof NavigationRoot) {
                NavigationRoot navigationRoot = (NavigationRoot) navigationItem;
                fileItemMap.put(navigationRoot.getFile(), navigationRoot);
            }
        }
        setValue(fileItemMap);
    }
}
