/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.theme.custom.CustomThemeColor;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.util.NightModeHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The following may end up passing the same lambda instance to the observer because it has
        // no capture, and result in an IllegalArgumentException "Cannot add the same observer with
        // different lifecycles" if activity is finished and instantly started again. To work around
        // this, always use an instance method reference.
        // https://stackoverflow.com/a/27524543
        //SettingsLiveDatas.PRIMARY_COLOR.observe(this, primaryColor -> CustomThemeHelper.sync());
        //SettingsLiveDatas.ACCENT_COLOR.observe(this, accentColor -> CustomThemeHelper.sync());
        //SettingsLiveDatas.NIGHT_MODE.observe(this, nightMode -> NightModeHelper.sync());
        SettingsLiveDatas.PRIMARY_COLOR.observe(this, this::onCustomThemeColorChanged);
        SettingsLiveDatas.ACCENT_COLOR.observe(this, this::onCustomThemeColorChanged);
        SettingsLiveDatas.NIGHT_MODE.observe(this, this::onNightModeChanged);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState,
                                       @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    private void onCustomThemeColorChanged(@NonNull CustomThemeColor color) {
        CustomThemeHelper.sync();
    }

    private void onNightModeChanged(@NonNull NightMode nightMode) {
        NightModeHelper.sync();
    }
}
