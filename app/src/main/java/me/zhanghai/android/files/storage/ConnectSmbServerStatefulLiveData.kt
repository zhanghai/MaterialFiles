/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.StatefulLiveData
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException

class ConnectSmbServerStatefulLiveData : StatefulLiveData<SmbServer>() {
    fun connect(server: SmbServer) {
        check(isReady)
        value = Loading(server)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                val path = server.path
                SmbServerAuthenticator.addTransientServer(server)
                try {
                    path.newDirectoryStream().toList()
                } finally {
                    SmbServerAuthenticator.removeTransientServer(server)
                    path.fileSystem.close()
                }
            } catch (e: IOException) {
                postValue(Failure(server, e))
                return@execute
            }
            postValue(Success(server))
        }
    }
}
