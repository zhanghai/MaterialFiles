/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import java8.nio.file.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.util.ActionState
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.isFinished
import me.zhanghai.android.files.util.isReady
import me.zhanghai.android.files.util.valueCompat

class TextEditorViewModel : ViewModel() {
    private val pathLiveData = MutableLiveData<Path>()
    var path: Path
        get() = pathLiveData.valueCompat
        set(value) {
            if (pathLiveData.valueCompat != value) {
                pathLiveData.value = value
            }
        }

    fun reload() {
        pathLiveData.value = pathLiveData.valueCompat
    }

    val fileContentLiveData = pathLiveData.switchMap { FileContentLiveData(it) }
    val fileContentStateful: Stateful<ByteArray>
        get() = fileContentLiveData.valueCompat

    private val _textChangedLiveData = MutableLiveData(false)
    val textChangedLiveData: LiveData<Boolean>
        get() = _textChangedLiveData
    var isTextChanged: Boolean
        get() = _textChangedLiveData.valueCompat
        set(changed) {
            if (fileContentStateful !is Success) {
                // Might happen if the animation is running and user is quick enough.
                return
            }
            _textChangedLiveData.value = changed
        }

    private val _writeFileState =
        MutableStateFlow<ActionState<Pair<Path, ByteArray>, Unit>>(ActionState.Ready())
    val writeFileState = _writeFileState.asStateFlow()

    fun writeFile(path: Path, content: ByteArray, context: Context) {
        viewModelScope.launch {
            check(_writeFileState.value.isReady)
            val argument = path to content
            _writeFileState.value = ActionState.Running(argument)
            FileJobService.write(path, content, context) {
                _writeFileState.value = if (it) {
                    ActionState.Success(argument, Unit)
                } else {
                    // The error will be toasted by service so we should never show it in UI, but we
                    // need a non-null value here.
                    ActionState.Error(argument, Throwable())
                }
            }
        }
    }

    fun finishWritingFile() {
        viewModelScope.launch {
            check(_writeFileState.value.isFinished)
            _writeFileState.value = ActionState.Ready()
        }
    }

    private var editTextSavedState: Parcelable? = null

    fun setEditTextSavedState(editTextSavedState: Parcelable?) {
        this.editTextSavedState = editTextSavedState
    }

    fun removeEditTextSavedState(): Parcelable? {
        val savedState = editTextSavedState
        editTextSavedState = null
        return savedState
    }
}
