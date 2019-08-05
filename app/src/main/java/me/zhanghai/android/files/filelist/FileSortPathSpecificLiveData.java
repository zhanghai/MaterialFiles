/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
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

public class FileSortPathSpecificLiveData extends MediatorLiveData<Boolean> {

    private SettingLiveData<FileSortOptions> mPathSortOptionsLiveData;

    public FileSortPathSpecificLiveData(@NonNull LiveData<Path> pathLiveData) {
        addSource(pathLiveData, path -> {
            if (mPathSortOptionsLiveData != null) {
                removeSource(mPathSortOptionsLiveData);
            }
            mPathSortOptionsLiveData = PathSettings.getFileListSortOptions(path);
            addSource(mPathSortOptionsLiveData, sortOptions -> loadValue());
        });
    }

    private void loadValue() {
        boolean value = mPathSortOptionsLiveData.getValue() != null;
        if (!Objects.equals(getValue(), value)) {
            setValue(value);
        }
    }

    public void putValue(boolean value) {
        if (value) {
            if (mPathSortOptionsLiveData.getValue() == null) {
                mPathSortOptionsLiveData.putValue(
                        Settings.FILE_LIST_SORT_OPTIONS.getValue());
            }
        } else {
            if (mPathSortOptionsLiveData.getValue() != null) {
                mPathSortOptionsLiveData.putValue(null);
            }
        }
    }
}
