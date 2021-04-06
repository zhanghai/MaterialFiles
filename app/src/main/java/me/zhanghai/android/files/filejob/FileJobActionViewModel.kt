/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import me.zhanghai.android.files.provider.common.PosixFileStore
import me.zhanghai.android.files.util.ActionState
import me.zhanghai.android.files.util.isFinished
import me.zhanghai.android.files.util.isReady

class FileJobActionViewModel : ViewModel() {
    private val _remountState =
        MutableStateFlow<ActionState<PosixFileStore, Unit>>(ActionState.Ready())
    val remountState = _remountState.asStateFlow()

    fun remount(fileStore: PosixFileStore) {
        viewModelScope.launch {
            check(_remountState.value.isReady)
            _remountState.value = ActionState.Running(fileStore)
            _remountState.value = try {
                runInterruptible(Dispatchers.IO) {
                    fileStore.isReadOnly = false
                }
                ActionState.Success(fileStore, Unit)
            } catch (e: Exception) {
                ActionState.Error(fileStore, e)
            }
        }
    }

    fun finishRemounting() {
        viewModelScope.launch {
            check(_remountState.value.isFinished)
            _remountState.value = ActionState.Ready()
        }
    }
}
