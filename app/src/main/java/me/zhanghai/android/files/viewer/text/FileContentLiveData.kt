/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.readAllBytes
import me.zhanghai.android.files.provider.common.size
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException

class FileContentLiveData(private val path: Path) : LiveData<Stateful<ByteArray>>() {
    init {
        loadValue()
    }

    private fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                if (path.size() > MAX_SIZE) {
                    throw IOException("File is too large")
                }
                val content = path.readAllBytes()
                Success(content)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    companion object {
        private const val MAX_SIZE = 1024 * 1024.toLong()
    }
}
