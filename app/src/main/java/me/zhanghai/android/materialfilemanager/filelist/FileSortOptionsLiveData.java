/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.MediatorLiveData;

import javax.annotation.Nullable;

import me.zhanghai.android.materialfilemanager.settings.SettingsLiveDatas;

public class FileSortOptionsLiveData extends MediatorLiveData<FileSortOptions> {

    @Nullable
    private static FileSortOptionsLiveData sInstance;

    public static FileSortOptionsLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new FileSortOptionsLiveData();
        }
        return sInstance;
    }

    private FileSortOptionsLiveData() {
        addSource(SettingsLiveDatas.FILE_LIST_SORT_BY, by -> loadValue());
        addSource(SettingsLiveDatas.FILE_LIST_SORT_ORDER, order -> loadValue());
        addSource(SettingsLiveDatas.FILE_LIST_SORT_DIRECTORIES_FIRST, directoryFirst ->
                loadValue());
    }

    private void loadValue() {
        FileSortOptions fileSortOptions = new FileSortOptions(
                SettingsLiveDatas.FILE_LIST_SORT_BY.getValue(),
                SettingsLiveDatas.FILE_LIST_SORT_ORDER.getValue(),
                SettingsLiveDatas.FILE_LIST_SORT_DIRECTORIES_FIRST.getValue());
        setValue(fileSortOptions);
    }

    public void putBy(FileSortOptions.By by) {
        SettingsLiveDatas.FILE_LIST_SORT_BY.putValue(by);
    }

    public void putOrder(FileSortOptions.Order order) {
        SettingsLiveDatas.FILE_LIST_SORT_ORDER.putValue(order);
    }

    public void putDirectoriesFirst(boolean directoriesFirst) {
        SettingsLiveDatas.FILE_LIST_SORT_DIRECTORIES_FIRST.putValue(directoriesFirst);
    }
}
