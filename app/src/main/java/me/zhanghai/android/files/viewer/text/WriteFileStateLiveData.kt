/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Context
import java8.nio.file.Path
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.util.StateData
import me.zhanghai.android.files.util.StateLiveData

class WriteFileStateLiveData : StateLiveData() {
    fun write(path: Path, content: ByteArray, context: Context) {
        checkReady()
        value = StateData.ofLoading()
        FileJobService.write(path, content, context) {
            value = if (it) StateData.ofSuccess() else StateData.ofError(null)
        }
    }
}
