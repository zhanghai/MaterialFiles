/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.filelist.FileSortOptions

object PathSettings {
    private const val NAME_SUFFIX = "path"

    @Suppress("UNCHECKED_CAST")
    fun getFileListSortOptions(path: Path): SettingLiveData<FileSortOptions?> =
        ParcelValueSettingLiveData(
            NAME_SUFFIX, R.string.pref_key_file_list_sort_options, path.toString(), null
        )
}
