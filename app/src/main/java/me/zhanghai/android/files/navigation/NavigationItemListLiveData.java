/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.settings.Settings;

public class NavigationItemListLiveData extends MediatorLiveData<List<NavigationItem>> {

    @Nullable
    private static NavigationItemListLiveData sInstance;

    @NonNull
    public static NavigationItemListLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new NavigationItemListLiveData();
        }
        return sInstance;
    }

    private NavigationItemListLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
        addSource(StandardDirectoriesLiveData.getInstance(), standardDirectories -> loadValue());
        addSource(Settings.BOOKMARK_DIRECTORIES, bookmarkDirectories -> loadValue());
        addSource(DocumentTreesLiveData.getInstance(), treeUris -> loadValue());
    }

    private void loadValue() {
        List<NavigationItem> navigationItems = NavigationItems.getItems();
        setValue(navigationItems);
    }
}
