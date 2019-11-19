/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.theme.custom.CustomThemeColor;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.theme.night.NightMode;
import me.zhanghai.android.files.theme.night.NightModeHelper;

public class SettingsPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
        // The following may end up passing the same lambda instance to the observer because it has
        // no capture, and result in an IllegalArgumentException "Cannot add the same observer with
        // different lifecycles" if activity is finished and instantly started again. To work around
        // this, always use an instance method reference.
        // https://stackoverflow.com/a/27524543
        //Settings.PRIMARY_COLOR.observe(viewLifecycleOwner,
        //        primaryColor -> CustomThemeHelper.sync());
        //Settings.ACCENT_COLOR.observe(viewLifecycleOwner,
        //        accentColor -> CustomThemeHelper.sync());
        //Settings.MATERIAL_DESIGN_2.observe(viewLifecycleOwner,
        //        enabled -> CustomThemeHelper.sync());
        //Settings.NIGHT_MODE.observe(viewLifecycleOwner, nightMode -> NightModeHelper.sync());
        Settings.PRIMARY_COLOR.observe(viewLifecycleOwner, this::onCustomThemeColorChanged);
        Settings.ACCENT_COLOR.observe(viewLifecycleOwner, this::onCustomThemeColorChanged);
        Settings.MATERIAL_DESIGN_2.observe(viewLifecycleOwner, this::onMaterialDesign2Changed);
        Settings.NIGHT_MODE.observe(viewLifecycleOwner, this::onNightModeChanged);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState,
                                       @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    private void onCustomThemeColorChanged(@NonNull CustomThemeColor color) {
        CustomThemeHelper.sync();
    }

    private void onMaterialDesign2Changed(boolean enabled) {
        CustomThemeHelper.sync();
    }

    private void onNightModeChanged(@NonNull NightMode nightMode) {
        NightModeHelper.sync();
    }
}
