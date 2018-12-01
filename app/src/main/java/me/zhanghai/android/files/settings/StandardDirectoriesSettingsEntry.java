/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.navigation.NavigationItems;
import me.zhanghai.android.files.navigation.StandardDirectory;

public class StandardDirectoriesSettingsEntry
        extends SettingsEntries.TypedListSettingsEntry<StandardDirectory> {

    public StandardDirectoriesSettingsEntry(int keyResId) {
        super(keyResId, StandardDirectory.CREATOR);
    }

    @NonNull
    private List<StandardDirectory> getDefaultTypedListValue() {
        return NavigationItems.getDefaultStandardDirectories();
    }

    @NonNull
    @Override
    public List<StandardDirectory> getTypedListValue() {
        List<StandardDirectory> value = super.getTypedListValue();
        if (value == null) {
            value = getDefaultTypedListValue();
        }
        return value;
    }
}
