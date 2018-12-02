/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileSortOptions;
import me.zhanghai.android.files.filelist.OpenApkDefaultAction;
import me.zhanghai.android.files.settings.SettingsEntries.BooleanSettingsEntry;
import me.zhanghai.android.files.settings.SettingsEntries.EnumSettingsEntry;
import me.zhanghai.android.files.settings.SettingsEntries.StringSettingsEntry;

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

    BooleanSettingsEntry FILE_LIST_ANIMATION = new BooleanSettingsEntry(
            R.string.pref_key_file_list_animation, R.bool.pref_default_value_file_list_animation);

    EnumSettingsEntry<NightMode> NIGHT_MODE = new EnumSettingsEntry<>(R.string.pref_key_night_mode,
            R.string.pref_default_value_night_mode, NightMode.class);

    StandardDirectoriesSettingsEntry STANDARD_DIRECTORIES = new StandardDirectoriesSettingsEntry(
            R.string.pref_key_standard_directories);

    StringSettingsEntry ZIP_FILE_NAME_ENCODING = new StringSettingsEntry(
            R.string.pref_key_zip_file_name_encoding,
            R.string.pref_default_value_zip_file_name_encoding);

    EnumSettingsEntry<OpenApkDefaultAction> OPEN_APK_DEFAULT_ACTION = new EnumSettingsEntry<>(
            R.string.pref_key_open_apk_default_action,
            R.string.pref_default_value_open_apk_default_action, OpenApkDefaultAction.class);
}
