/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

import android.support.v7.app.AppCompatDelegate;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filelist.FileSortOptions;
import me.zhanghai.android.materialfilemanager.filelist.OpenApkDefaultAction;
import me.zhanghai.android.materialfilemanager.settings.SettingsEntries.*;

interface Settings {

    BooleanSettingsEntry FILE_LIST_SHOW_HIDDEN_FILES = new BooleanSettingsEntry(
            R.string.pref_key_file_list_show_hidden_files,
            R.bool.pref_default_value_file_list_show_hidden_files);

    EnumSettingsEntry<FileSortOptions.By> FILE_LIST_SORT_BY = new EnumSettingsEntry<>(
            R.string.pref_key_file_list_sort_by, R.string.pref_default_value_file_list_sort_by,
            FileSortOptions.By.class);

    EnumSettingsEntry<FileSortOptions.Order> FILE_LIST_SORT_ORDER = new EnumSettingsEntry<>(
            R.string.pref_key_file_list_sort_order,
            R.string.pref_default_value_file_list_sort_order, FileSortOptions.Order.class);

    BooleanSettingsEntry FILE_LIST_SORT_DIRECTORIES_FIRST = new BooleanSettingsEntry(
            R.string.pref_key_file_list_sort_directories_first,
            R.bool.pref_default_value_file_list_sort_directories_first);

    enum NightMode {

        // Disabled because AppCompatDelegate delegates night mode change to the non-existent system
        // implementation.
        FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        OFF(AppCompatDelegate.MODE_NIGHT_NO),
        ON(AppCompatDelegate.MODE_NIGHT_YES),
        AUTO(AppCompatDelegate.MODE_NIGHT_AUTO);

        private int value;

        NightMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    EnumSettingsEntry<NightMode> NIGHT_MODE = new EnumSettingsEntry<>(R.string.pref_key_night_mode,
            R.string.pref_default_value_night_mode, NightMode.class);

    StringSettingsEntry ZIP_FILE_NAME_ENCODING = new StringSettingsEntry(
            R.string.pref_key_zip_file_name_encoding,
            R.string.pref_default_value_zip_file_name_encoding);

    EnumSettingsEntry<OpenApkDefaultAction> OPEN_APK_DEFAULT_ACTION = new EnumSettingsEntry<>(
            R.string.pref_key_open_apk_default_action,
            R.string.pref_default_value_open_apk_default_action, OpenApkDefaultAction.class);
}
