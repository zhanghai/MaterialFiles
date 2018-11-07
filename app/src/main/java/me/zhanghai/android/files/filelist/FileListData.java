/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.filesystem.File;

public class FileListData {

    public enum State {
        LOADING,
        ERROR,
        SUCCESS
    }

    @NonNull
    public final State state;
    @NonNull
    public final File file;
    @Nullable
    public final List<File> fileList;
    @Nullable
    public final Exception exception;

    private FileListData(@NonNull State state, @NonNull File file, @Nullable List<File> fileList,
                         @Nullable Exception exception) {
        this.state = state;
        this.file = file;
        this.fileList = fileList;
        this.exception = exception;
    }

    @NonNull
    public static FileListData ofLoading(@NonNull File file) {
        return new FileListData(State.LOADING, file, null, null);
    }

    @NonNull
    public static FileListData ofError(@NonNull File file, @NonNull Exception exception) {
        return new FileListData(State.ERROR, file, null, exception);
    }

    @NonNull
    public static FileListData ofSuccess(@NonNull File file, @NonNull List<File> fileList) {
        return new FileListData(State.SUCCESS, file, fileList, null);
    }
}
