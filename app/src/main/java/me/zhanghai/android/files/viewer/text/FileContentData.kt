/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import java8.nio.file.Path
import me.zhanghai.android.files.util.StatefulData

class FileContentData private constructor(
    state: State,
    val path: Path,
    data: ByteArray?,
    exception: Exception?
) : StatefulData<ByteArray?>(state, data, exception) {
    companion object {
        fun ofLoading(path: Path): FileContentData =
            FileContentData(State.LOADING, path, null, null)

        fun ofError(path: Path, exception: Exception): FileContentData =
            FileContentData(State.ERROR, path, null, exception)

        fun ofSuccess(path: Path, content: ByteArray): FileContentData =
            FileContentData(State.SUCCESS, path, content, null)
    }
}
