/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import java8.nio.file.Path;
import me.zhanghai.android.files.settings.PathSettings;
import me.zhanghai.android.files.settings.SettingLiveData;
import me.zhanghai.android.files.settings.Settings;

public class FileSortOptionsLiveData extends MediatorLiveData<FileSortOptions> {

    private SettingLiveData<FileSortOptions> mPathSortOptionsLiveData;

    public FileSortOptionsLiveData(@NonNull LiveData<Path> pathLiveData) {
        addSource(Settings.FILE_LIST_SORT_OPTIONS, sortOptions -> loadValue());
        addSource(pathLiveData, path -> {
            if (mPathSortOptionsLiveData != null) {
                removeSource(mPathSortOptionsLiveData);
            }
            mPathSortOptionsLiveData = PathSettings.getFileListSortOptions(path);
            addSource(mPathSortOptionsLiveData, sortOptions -> loadValue());
        });
    }

    private void loadValue() {
        if (mPathSortOptionsLiveData == null) {
            // Not yet initialized.
            return;
        }
        FileSortOptions value = mPathSortOptionsLiveData.getValue();
        if (value == null) {
            value = Settings.FILE_LIST_SORT_OPTIONS.getValue();
        }
        if (!Objects.equals(getValue(), value)) {
            setValue(value);
        }
    }

    public void putBy(@NonNull FileSortOptions.By by) {
        putValue(getValue().withBy(by));
    }

    public void putOrder(@NonNull FileSortOptions.Order order) {
        putValue(getValue().withOrder(order));
    }

    public void putDirectoriesFirst(boolean directoriesFirst) {
        putValue(getValue().withDirectoriesFirst(directoriesFirst));
    }

    private void putValue(@NonNull FileSortOptions value) {
        if (mPathSortOptionsLiveData.getValue() != null) {
            mPathSortOptionsLiveData.putValue(value);
        } else {
            Settings.FILE_LIST_SORT_OPTIONS.putValue(value);
        }
    }
}
