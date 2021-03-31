/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.util.ActionState

class EditSmbServerViewModel : ViewModel() {
    private val _connectState = MutableStateFlow<ActionState<SmbServer, Unit>>(ActionState.Ready())
    val connectState = _connectState.asStateFlow()

    fun connect(server: SmbServer) {
        viewModelScope.launch {
            check(_connectState.value is ActionState.Ready)
            _connectState.value = ActionState.Running(server)
            try {
                runInterruptible(Dispatchers.IO) {
                    SmbServerAuthenticator.addTransientServer(server)
                    try {
                        val path = server.path
                        path.fileSystem.use {
                            path.newDirectoryStream().toList()
                        }
                    } finally {
                        SmbServerAuthenticator.removeTransientServer(server)
                    }
                }
                _connectState.value = ActionState.Success(server, Unit)
            } catch (e: Exception) {
                _connectState.value = ActionState.Error(server, e)
            }
        }
    }

    fun finishConnecting() {
        viewModelScope.launch {
            _connectState.value = ActionState.Ready()
        }
    }
}
