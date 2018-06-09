/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filelist.FileSortOptions;
import me.zhanghai.android.materialfilemanager.settings.SettingsEntries.*;

public interface Settings {

    EnumSettingsEntry<FileSortOptions.By> FILE_LIST_SORT_BY = new EnumSettingsEntry<>(
            R.string.pref_key_file_list_sort_by, R.string.pref_default_value_file_list_sort_by,
            FileSortOptions.By.class);

    EnumSettingsEntry<FileSortOptions.Order> FILE_LIST_SORT_ORDER = new EnumSettingsEntry<>(
            R.string.pref_key_file_list_sort_order,
            R.string.pref_default_value_file_list_sort_order, FileSortOptions.Order.class);

    BooleanSettingsEntry FILE_LIST_SORT_DIRECTORIES_FIRST = new BooleanSettingsEntry(
            R.string.pref_key_file_list_sort_directories_first,
            R.bool.pref_default_value_file_list_sort_directories_first);
}
