/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import java8.nio.file.Path
import me.zhanghai.android.files.util.StatefulData
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
    val fileContentData: FileContentData
        get() = fileContentLiveData.valueCompat

    private val _textChangedLiveData = MutableLiveData(false)
    val textChangedLiveData: LiveData<Boolean>
        get() = _textChangedLiveData
    var isTextChanged: Boolean
        get() = _textChangedLiveData.valueCompat
        set(changed) {
            if (fileContentLiveData.valueCompat.state !== StatefulData.State.SUCCESS) {
                // Might happen if the animation is running and user is quick enough.
                return
            }
            _textChangedLiveData.value = changed
        }

    val writeFileStateLiveData = WriteFileStateLiveData()

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
