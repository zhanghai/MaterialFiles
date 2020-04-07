/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.os.Bundle
import android.view.View
import com.takisoft.preferencex.PreferenceFragmentCompat

/**
 * https://github.com/Gericop/Android-Support-Preference-V7-Fix/issues/201
 */
abstract class PreferenceFragmentCompatFixIssue201 : PreferenceFragmentCompat() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (preferenceScreen == null) {
            val preferenceManager = preferenceManager
            val context = preferenceManager.context
            val preferenceScreen = preferenceManager.createPreferenceScreen(context)
            setPreferenceScreen(preferenceScreen)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
