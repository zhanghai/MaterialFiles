/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import androidx.lifecycle.ViewModel

class EditSmbServerViewModel : ViewModel() {
    val connectStatefulLiveData = ConnectSmbServerStateLiveData()
}
