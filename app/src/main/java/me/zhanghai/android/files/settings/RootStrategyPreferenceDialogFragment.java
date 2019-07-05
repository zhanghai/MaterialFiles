/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import androidx.preference.PreferenceDialogFragmentCompat;

public class RootStrategyPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    @Override
    public void onDialogClosed(boolean positiveResult) {
        RootStrategyPreference preference = (RootStrategyPreference) getPreference();
        if (positiveResult) {
            preference.confirmClick();
        }
    }
}
