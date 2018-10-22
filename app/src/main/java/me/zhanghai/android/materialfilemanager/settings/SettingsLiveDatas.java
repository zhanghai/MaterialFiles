/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

public interface SettingsLiveDatas {

    SettingsLiveData<Boolean> FILE_LIST_SHOW_HIDDEN_FILES = new SettingsLiveData<>(
            Settings.FILE_LIST_SHOW_HIDDEN_FILES);
}
