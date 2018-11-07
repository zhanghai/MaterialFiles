/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.filesystem.File;

public class NavigationRootMapLiveData extends MediatorLiveData<Map<File, NavigationRoot>> {

    @Nullable
    private static NavigationRootMapLiveData sInstance;

    @NonNull
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
