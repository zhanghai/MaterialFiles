/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import me.zhanghai.android.files.filelist.FileSortOptions;
import me.zhanghai.android.files.filelist.OpenApkDefaultAction;
import me.zhanghai.android.files.provider.root.RootStrategy;

public interface SettingsLiveDatas {

    SettingsLiveData<Boolean> FILE_LIST_SHOW_HIDDEN_FILES = new SettingsLiveData<>(
            Settings.FILE_LIST_SHOW_HIDDEN_FILES);

    EnumSettingsLiveData<FileSortOptions.By> FILE_LIST_SORT_BY = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_BY);

    EnumSettingsLiveData<FileSortOptions.Order> FILE_LIST_SORT_ORDER = new EnumSettingsLiveData<>(
            Settings.FILE_LIST_SORT_ORDER);

    SettingsLiveData<Boolean> FILE_LIST_SORT_DIRECTORIES_FIRST = new SettingsLiveData<>(
            Settings.FILE_LIST_SORT_DIRECTORIES_FIRST);

    ResourceIdSettingsLiveData CREATE_ARCHIVE_TYPE = new ResourceIdSettingsLiveData(
            Settings.CREATE_ARCHIVE_TYPE);

    SettingsLiveData<Boolean> FILE_LIST_ANIMATION = new SettingsLiveData<>(
            Settings.FILE_LIST_ANIMATION);

    EnumSettingsLiveData<NightMode> NIGHT_MODE = new EnumSettingsLiveData<>(
            Settings.NIGHT_MODE);

    TypedListSettingsLiveData<StandardDirectorySettings> STANDARD_DIRECTORY_SETTINGS =
            new TypedListSettingsLiveData<>(Settings.STANDARD_DIRECTORY_SETTINGS);

    EnumSettingsLiveData<RootStrategy> ROOT_STRATEGY = new EnumSettingsLiveData<>(
            Settings.ROOT_STRATEGY);

    SettingsLiveData<String> ARCHIVE_FILE_NAME_ENCODING = new SettingsLiveData<>(
            Settings.ARCHIVE_FILE_NAME_ENCODING);

    EnumSettingsLiveData<OpenApkDefaultAction> OPEN_APK_DEFAULT_ACTION = new EnumSettingsLiveData<>(
            Settings.OPEN_APK_DEFAULT_ACTION);
}
