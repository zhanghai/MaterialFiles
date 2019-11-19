/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import me.zhanghai.android.files.navigation.NavigationItems;
import me.zhanghai.android.files.navigation.StandardDirectoriesLiveData;
import me.zhanghai.android.files.navigation.StandardDirectory;
import me.zhanghai.android.files.navigation.StandardDirectorySettings;
import me.zhanghai.android.files.util.ViewUtils;
import me.zhanghai.java.functional.Functional;

public class StandardDirectoriesPreferenceFragment extends PreferenceFragmentCompatFixIssue201
        implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState,
                                       @Nullable String rootKey) {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        StandardDirectoriesLiveData.getInstance().observe(getViewLifecycleOwner(),
                this::onStandardDirectoriesChanged);
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
            String id = standardDirectory.getId();
            SwitchPreferenceCompat preference = (SwitchPreferenceCompat) oldPreferences.get(id);
            if (preference == null) {
                preference = new SwitchPreferenceCompat(context);
                preference.setKey(id);
                preference.setPersistent(false);
                preference.setOnPreferenceClickListener(this);
            }
            Drawable icon = AppCompatResources.getDrawable(context, standardDirectory.getIconRes());
            icon.mutate();
            DrawableCompat.setTint(icon, secondaryTextColor);
            preference.setIcon(icon);
            preference.setTitle(standardDirectory.getTitle(context));
            preference.setSummary(NavigationItems.getExternalStorageDirectory(
                    standardDirectory.getRelativePath()));
            preference.setChecked(standardDirectory.isEnabled());
            preferenceScreen.addPreference(preference);
        }
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) preference;
        String id = switchPreference.getKey();
        boolean enabled = switchPreference.isChecked();
        List<StandardDirectorySettings> settingsList = new ArrayList<>(
                Settings.STANDARD_DIRECTORY_SETTINGS.getValue());
        int index = Functional.findIndex(settingsList, settings -> Objects.equals(settings.getId(),
                id));
        if (index != -1) {
            StandardDirectorySettings settings = settingsList.get(index);
            settings = settings.withEnabled(enabled);
            settingsList.set(index, settings);
        } else {
            List<StandardDirectory> standardDirectories =
                    StandardDirectoriesLiveData.getInstance().getValue();
            StandardDirectory standardDirectory = Functional.find(standardDirectories,
                    standardDirectory2 -> Objects.equals(standardDirectory2.getId(), id));
            StandardDirectorySettings settings = standardDirectory.toSettings().withEnabled(
                    enabled);
            settingsList.add(settings);
        }
        Settings.STANDARD_DIRECTORY_SETTINGS.putValue(settingsList);
        return true;
    }
}
