/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.Stateful

class FilePropertiesViewModel(file: FileItem) : ViewModel() {
    private val _fileLiveData = FileLiveData(file)
    val fileLiveData: LiveData<Stateful<FileItem>>
        get() = _fileLiveData

    fun reloadFile() {
        _fileLiveData.loadValue()
    }

    override fun onCleared() {
        _fileLiveData.close()
    }
}
