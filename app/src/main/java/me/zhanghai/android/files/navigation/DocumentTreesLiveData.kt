/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import androidx.lifecycle.LiveData
import me.zhanghai.android.files.file.DocumentTreeUri

object DocumentTreesLiveData : LiveData<List<DocumentTreeUri>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
    }

    fun loadValue() {
        value = DocumentTreeUri.persistedUris
    }
}
