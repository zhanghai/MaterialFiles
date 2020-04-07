/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.StatefulData

class FileData private constructor(
    state: State, data: FileItem?,
    exception: Exception?
) : StatefulData<FileItem>(state, data, exception) {
    companion object {
        fun ofLoading(file: FileItem? = null): FileData =
            FileData(State.LOADING, file, null)

        fun ofError(exception: Exception): FileData =
            FileData(State.ERROR, null, exception)

        fun ofSuccess(file: FileItem): FileData =
            FileData(State.SUCCESS, file, null)
    }
}
