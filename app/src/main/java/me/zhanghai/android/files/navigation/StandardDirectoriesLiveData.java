/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.settings.Settings;

public class StandardDirectoriesLiveData extends MediatorLiveData<List<StandardDirectory>> {

    @Nullable
    private static StandardDirectoriesLiveData sInstance;

    @NonNull
    public static StandardDirectoriesLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new StandardDirectoriesLiveData();
        }
        return sInstance;
    }

    private StandardDirectoriesLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
        addSource(Settings.STANDARD_DIRECTORY_SETTINGS, standardDirectorySettings ->
                loadValue());
    }

    private void loadValue() {
        List<StandardDirectory> standardDirectories = NavigationItems.getStandardDirectories();
        setValue(standardDirectories);
    }
}
