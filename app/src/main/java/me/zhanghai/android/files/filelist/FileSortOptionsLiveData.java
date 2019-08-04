/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import me.zhanghai.android.files.settings.Settings;

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
        addSource(Settings.FILE_LIST_SORT_OPTIONS, this::setValue);
    }

    public void putBy(@NonNull FileSortOptions.By by) {
        Settings.FILE_LIST_SORT_OPTIONS.putValue(
                Settings.FILE_LIST_SORT_OPTIONS.getValue().withBy(by));
    }

    public void putOrder(@NonNull FileSortOptions.Order order) {
        Settings.FILE_LIST_SORT_OPTIONS.putValue(
                Settings.FILE_LIST_SORT_OPTIONS.getValue().withOrder(order));
    }

    public void putDirectoriesFirst(boolean directoriesFirst) {
        Settings.FILE_LIST_SORT_OPTIONS.putValue(
                Settings.FILE_LIST_SORT_OPTIONS.getValue().withDirectoriesFirst(directoriesFirst));
    }
}
