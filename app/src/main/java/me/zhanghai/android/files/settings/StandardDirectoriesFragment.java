/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.navigation.StandardDirectory;
import me.zhanghai.android.files.util.ViewUtils;

public class StandardDirectoriesFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState,
                                       @Nullable String rootKey) {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingsLiveDatas.STANDARD_DIRECTORIES.observe(this, this::onStandardDirectoriesChanged);
    }

    private void onStandardDirectoriesChanged(
            @NonNull List<StandardDirectory> standardDirectories) {

        PreferenceManager preferenceManager = getPreferenceManager();
        Context context = preferenceManager.getContext();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Map<String, Preference> oldPreferences = new HashMap<>();
        if (preferenceScreen == null) {
            preferenceScreen = preferenceManager.createPreferenceScreen(context);
            setPreferenceScreen(preferenceScreen);
        } else {
            for (int i = preferenceScreen.getPreferenceCount() - 1; i >= 0; --i) {
                Preference preference = preferenceScreen.getPreference(i);
                preferenceScreen.removePreference(preference);
                oldPreferences.put(preference.getKey(), preference);
            }
        }

        int secondaryTextColor = ViewUtils.getColorFromAttrRes(android.R.attr.textColorSecondary, 0,
                context);
        for (StandardDirectory standardDirectory : standardDirectories) {
            String key = standardDirectory.getRelativePath();
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) oldPreferences.get(key);
            if (preference == null) {
                preference = new SwitchPreferenceCompat(context);
                preference.setKey(key);
                preference.setPersistent(false);
                preference.setOnPreferenceClickListener(this);
            }
            Drawable icon = AppCompatResources.getDrawable(context, standardDirectory.getIconRes());
            icon.mutate();
            DrawableCompat.setTint(icon, secondaryTextColor);
            preference.setIcon(icon);
            preference.setTitle(standardDirectory.getTitle(context));
            preference.setChecked(standardDirectory.isEnabled());
            preferenceScreen.addPreference(preference);
        }
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
        String relativePath = switchPreference.getKey();
        List<StandardDirectory> standardDirectories =
                SettingsLiveDatas.STANDARD_DIRECTORIES.getValue();
        StandardDirectory standardDirectory = Functional.find(standardDirectories,
                standardDirectory2 -> TextUtils.equals(standardDirectory2.getRelativePath(),
                        relativePath));
        standardDirectory.setEnabled(switchPreference.isChecked());
        SettingsLiveDatas.STANDARD_DIRECTORIES.putValue(standardDirectories);
        return true;
    }
}
