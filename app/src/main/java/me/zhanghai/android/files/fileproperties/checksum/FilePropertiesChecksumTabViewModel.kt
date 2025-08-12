/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksum

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import me.zhanghai.android.files.util.Stateful

class FilePropertiesChecksumTabViewModel(path: Path) : ViewModel() {
    private val _checksumInfoLiveData = ChecksumInfoLiveData(path)
    val checksumInfoLiveData: LiveData<Stateful<ChecksumInfo>>
        get() = _checksumInfoLiveData

    fun reload() {
        _checksumInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _checksumInfoLiveData.close()
    }
}
