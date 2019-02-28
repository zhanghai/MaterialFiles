/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.settings.SettingsLiveDatas;

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
