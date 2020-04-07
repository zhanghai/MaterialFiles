/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java8.nio.file.Path
import me.zhanghai.android.files.settings.PathSettings
import me.zhanghai.android.files.settings.SettingLiveData
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

class FileSortPathSpecificLiveData(pathLiveData: LiveData<Path>) : MediatorLiveData<Boolean>() {
    private lateinit var pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>

    private fun loadValue() {
        val value = pathSortOptionsLiveData.value != null
        if (this.value != value) {
            this.value = value
        }
    }

    fun putValue(value: Boolean) {
        if (value) {
            if (pathSortOptionsLiveData.value == null) {
                pathSortOptionsLiveData.putValue(Settings.FILE_LIST_SORT_OPTIONS.valueCompat)
            }
        } else {
            if (pathSortOptionsLiveData.value != null) {
                pathSortOptionsLiveData.putValue(null)
            }
        }
    }

    init {
        addSource(pathLiveData) { path: Path ->
            if (this::pathSortOptionsLiveData.isInitialized) {
                removeSource(pathSortOptionsLiveData)
            }
            pathSortOptionsLiveData = PathSettings.getFileListSortOptions(path)
            addSource(pathSortOptionsLiveData) { loadValue() }
        }
    }
}
