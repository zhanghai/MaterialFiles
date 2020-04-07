/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.os.Bundle
import androidx.lifecycle.observe
import com.takisoft.preferencex.PreferenceFragmentCompat
import me.zhanghai.android.files.R
import me.zhanghai.android.files.theme.custom.CustomThemeColor
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightMode
import me.zhanghai.android.files.theme.night.NightModeHelper

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewLifecycleOwner = viewLifecycleOwner
        // The following may end up passing the same lambda instance to the observer because it has
        // no capture, and result in an IllegalArgumentException "Cannot add the same observer with
        // different lifecycles" if activity is finished and instantly started again. To work around
        // this, always use an instance method reference.
        // https://stackoverflow.com/a/27524543
        //Settings.PRIMARY_COLOR.observe(viewLifecycleOwner) { CustomThemeHelper.sync() }
        //Settings.ACCENT_COLOR.observe(viewLifecycleOwner) { CustomThemeHelper.sync()) }
        //Settings.MATERIAL_DESIGN_2.observe(viewLifecycleOwner) { CustomThemeHelper.sync() }
        //Settings.NIGHT_MODE.observe(viewLifecycleOwner) { NightModeHelper.sync() }
        Settings.PRIMARY_COLOR.observe(viewLifecycleOwner, this::onCustomThemeColorChanged)
        Settings.ACCENT_COLOR.observe(viewLifecycleOwner, this::onCustomThemeColorChanged)
        Settings.MATERIAL_DESIGN_2.observe(viewLifecycleOwner, this::onMaterialDesign2Changed)
        Settings.NIGHT_MODE.observe(viewLifecycleOwner, this::onNightModeChanged)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    private fun onCustomThemeColorChanged(color: CustomThemeColor) {
        CustomThemeHelper.sync()
    }

    private fun onMaterialDesign2Changed(enabled: Boolean) {
        CustomThemeHelper.sync()
    }

    private fun onNightModeChanged(nightMode: NightMode) {
        NightModeHelper.sync()
    }
}
