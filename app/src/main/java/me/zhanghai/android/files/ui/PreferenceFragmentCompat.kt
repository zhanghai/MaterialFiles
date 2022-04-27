/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceFragmentCompat as TakisoftPreferenceFragmentCompat

abstract class PreferenceFragmentCompat : TakisoftPreferenceFragmentCompat() {
    // @see https://github.com/Gericop/Android-Support-Preference-V7-Fix/issues/201
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (preferenceScreen == null) {
            val preferenceManager = preferenceManager
            val context = preferenceManager.context
            val preferenceScreen = preferenceManager.createPreferenceScreen(context)
            setPreferenceScreen(preferenceScreen)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null
            && preference is ListPreference) {
            displayPreferenceDialog(MaterialListPreferenceDialogFragmentCompat(), preference.key)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        // @see PreferenceFragmentCompat.DIALOG_FRAGMENT_TAG
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}
