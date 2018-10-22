/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

import me.zhanghai.android.materialfilemanager.filelist.FileSortOptions;

public interface SettingsLiveDatas {

    SettingsLiveData<Boolean> FILE_LIST_SHOW_HIDDEN_FILES = new SettingsLiveData<>(
            Settings.FILE_LIST_SHOW_HIDDEN_FILES);

    EnumSettingsLiveData<FileSortOptions.By> FILE_LIST_SORT_BY = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_BY);

    EnumSettingsLiveData<FileSortOptions.Order> FILE_LIST_SORT_ORDER = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_ORDER);

    SettingsLiveData<Boolean> FILE_LIST_SORT_DIRECTORIES_FIRST = new SettingsLiveData<>(
            Settings.FILE_LIST_SORT_DIRECTORIES_FIRST);
}
