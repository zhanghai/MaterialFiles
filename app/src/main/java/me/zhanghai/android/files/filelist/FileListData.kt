/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.StatefulData

class FileListData private constructor(
    state: State,
    data: List<FileItem>?,
    exception: Exception?
) : StatefulData<List<FileItem>>(state, data, exception) {
    companion object {
        fun ofLoading(fileList: List<FileItem>? = null): FileListData {
            return FileListData(State.LOADING, fileList, null)
        }

        fun ofError(exception: Exception): FileListData {
            return FileListData(State.ERROR, null, exception)
        }

        fun ofSuccess(fileList: List<FileItem>): FileListData {
            return FileListData(State.SUCCESS, fileList, null)
        }
    }
}
