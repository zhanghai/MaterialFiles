/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.settings.SettingsLiveDatas;

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

    public NavigationItemListLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
        addSource(SettingsLiveDatas.STANDARD_DIRECTORIES, standardDirectories -> loadValue());
    }

    private void loadValue() {
        List<NavigationItem> navigationItems = NavigationItems.getItems();
        setValue(navigationItems);
    }
}
