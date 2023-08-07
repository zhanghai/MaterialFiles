/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
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

class FileViewTypeLiveData(pathLiveData: LiveData<Path>) : MediatorLiveData<FileViewType>() {
    private lateinit var pathViewTypeLiveData: SettingLiveData<FileViewType?>

    private fun loadValue() {
        if (!this::pathViewTypeLiveData.isInitialized) {
            // Not yet initialized.
            return
        }
        val value = pathViewTypeLiveData.value ?: Settings.FILE_LIST_VIEW_TYPE.valueCompat
        if (this.value != value) {
            this.value = value
        }
    }

    fun putValue(value: FileViewType) {
        if (pathViewTypeLiveData.value != null) {
            pathViewTypeLiveData.putValue(value)
        } else {
            Settings.FILE_LIST_VIEW_TYPE.putValue(value)
        }
    }

    init {
        addSource(Settings.FILE_LIST_VIEW_TYPE) { loadValue() }
        addSource(pathLiveData) { path: Path ->
            if (this::pathViewTypeLiveData.isInitialized) {
                removeSource(pathViewTypeLiveData)
            }
            pathViewTypeLiveData = PathSettings.getFileListViewType(path)
            addSource(pathViewTypeLiveData) { loadValue() }
        }
    }
}
