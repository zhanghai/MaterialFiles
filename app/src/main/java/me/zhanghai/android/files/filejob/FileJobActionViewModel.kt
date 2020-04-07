/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import androidx.lifecycle.ViewModel

class FileJobActionViewModel : ViewModel() {
    val remountStateLiveData = RemountStateLiveData()
}
