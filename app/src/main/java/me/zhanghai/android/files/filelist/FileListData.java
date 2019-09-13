/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.util.StatefulData;

public class FileListData extends StatefulData<List<FileItem>> {

    private FileListData(@NonNull State state, @Nullable List<FileItem> data,
                         @Nullable Exception exception) {
        super(state, data, exception);
    }

    @NonNull
    public static FileListData ofLoading(@Nullable List<FileItem> fileList) {
        return new FileListData(State.LOADING, fileList, null);
    }

    @NonNull
    public static FileListData ofLoading() {
        return ofLoading(null);
    }

    @NonNull
    public static FileListData ofError(@NonNull Exception exception) {
        return new FileListData(State.ERROR, null, exception);
    }

    @NonNull
    public static FileListData ofSuccess(@NonNull List<FileItem> fileList) {
        return new FileListData(State.SUCCESS, fileList, null);
    }
}
