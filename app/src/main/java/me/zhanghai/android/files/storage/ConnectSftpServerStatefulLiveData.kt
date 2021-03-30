/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.AsyncTask
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.StatefulLiveData
import me.zhanghai.android.files.util.Success
import java.io.IOException

class ConnectSftpServerStatefulLiveData : StatefulLiveData<SftpServer>() {
    fun connect(server: SftpServer) {
        check(isReady)
        value = Loading(server)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                val path = server.path
                SftpServerAuthenticator.addTransientServer(server)
                try {
                    path.newDirectoryStream().toList()
                } finally {
                    SftpServerAuthenticator.removeTransientServer(server)
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
