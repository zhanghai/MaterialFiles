/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileSortOptions;

public class PathSettings {

    private static final String NAME_SUFFIX = "path";

    private PathSettings() {}

    @NonNull
    public static SettingLiveData<FileSortOptions> getFileListSortOptions(@NonNull Path path) {
        return new SettingLiveDatas.ParcelableSettingLiveData<>(NAME_SUFFIX,
                R.string.pref_key_file_list_sort_options, path.toString(), null,
                FileSortOptions.class);
    }
}
