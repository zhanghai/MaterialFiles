/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import me.zhanghai.android.materialfilemanager.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState,
                                       @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
