/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import java8.nio.file.Path;

public class NavigationRootMapLiveData extends MediatorLiveData<Map<Path, NavigationRoot>> {

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
        Map<Path, NavigationRoot> fileItemMap = new HashMap<>();
        for (NavigationItem navigationItem : navigationItems) {
            if (navigationItem instanceof NavigationRoot) {
                NavigationRoot navigationRoot = (NavigationRoot) navigationItem;
                fileItemMap.put(navigationRoot.getPath(), navigationRoot);
            }
        }
        setValue(fileItemMap);
    }
}
