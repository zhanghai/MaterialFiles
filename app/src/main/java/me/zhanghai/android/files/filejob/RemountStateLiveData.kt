/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import android.os.AsyncTask
import me.zhanghai.android.files.provider.common.PosixFileStore
import me.zhanghai.android.files.util.StateData
import me.zhanghai.android.files.util.StateLiveData
import java.io.IOException

class RemountStateLiveData : StateLiveData() {
    fun remount(fileStore: PosixFileStore) {
        checkReady()
        value = StateData.ofLoading()
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                fileStore.isReadOnly = false
            } catch (e: IOException) {
                postValue(StateData.ofError(e))
                return@execute
            }
            postValue(StateData.ofSuccess())
        }
    }
}
