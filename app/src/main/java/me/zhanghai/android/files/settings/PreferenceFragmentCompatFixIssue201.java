/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

/**
 * https://github.com/Gericop/Android-Support-Preference-V7-Fix/issues/201
 */
public abstract class PreferenceFragmentCompatFixIssue201 extends PreferenceFragmentCompat {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getPreferenceScreen() == null) {
            PreferenceManager preferenceManager = getPreferenceManager();
            Context context = preferenceManager.getContext();
            PreferenceScreen preferenceScreen = preferenceManager.createPreferenceScreen(context);
            setPreferenceScreen(preferenceScreen);
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
