/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import androidx.lifecycle.ViewModel

class EditSftpServerViewModel : ViewModel() {
    val connectStatefulLiveData = ConnectSftpServerStatefulLiveData()
}
