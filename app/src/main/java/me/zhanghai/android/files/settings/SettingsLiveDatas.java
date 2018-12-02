/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import me.zhanghai.android.files.filelist.FileSortOptions;
import me.zhanghai.android.files.filelist.OpenApkDefaultAction;
import me.zhanghai.android.files.navigation.StandardDirectory;

public interface SettingsLiveDatas {

    SettingsLiveData<Boolean> FILE_LIST_SHOW_HIDDEN_FILES = new SettingsLiveData<>(
            Settings.FILE_LIST_SHOW_HIDDEN_FILES);

    EnumSettingsLiveData<FileSortOptions.By> FILE_LIST_SORT_BY = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_BY);

    EnumSettingsLiveData<FileSortOptions.Order> FILE_LIST_SORT_ORDER = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_ORDER);

    SettingsLiveData<Boolean> FILE_LIST_SORT_DIRECTORIES_FIRST = new SettingsLiveData<>(
            Settings.FILE_LIST_SORT_DIRECTORIES_FIRST);

    SettingsLiveData<Boolean> FILE_LIST_ANIMATION = new SettingsLiveData<>(
            Settings.FILE_LIST_ANIMATION);

    EnumSettingsLiveData<NightMode> NIGHT_MODE = new EnumSettingsLiveData<>(
            Settings.NIGHT_MODE);

    SettingsLiveData<String> ZIP_FILE_NAME_ENCODING = new SettingsLiveData<>(
            Settings.ZIP_FILE_NAME_ENCODING);

    EnumSettingsLiveData<OpenApkDefaultAction> OPEN_APK_DEFAULT_ACTION = new EnumSettingsLiveData<>(
            Settings.OPEN_APK_DEFAULT_ACTION);

    TypedListSettingsLiveData<StandardDirectory> STANDARD_DIRECTORIES =
            new TypedListSettingsLiveData<>(Settings.STANDARD_DIRECTORIES);
}
