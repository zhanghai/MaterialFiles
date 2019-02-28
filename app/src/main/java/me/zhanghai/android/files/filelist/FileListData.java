/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;

public class FileListData {

    public enum State {
        LOADING,
        ERROR,
        SUCCESS
    }

    @NonNull
    public final State state;
    @NonNull
    public final Path path;
    @Nullable
    public final List<FileItem> fileList;
    @Nullable
    public final Exception exception;

    private FileListData(@NonNull State state, @NonNull Path path,
                         @Nullable List<FileItem> fileList, @Nullable Exception exception) {
        this.state = state;
        this.path = path;
        this.fileList = fileList;
        this.exception = exception;
    }

    @NonNull
    public static FileListData ofLoading(@NonNull Path path) {
        return new FileListData(State.LOADING, path, null, null);
    }

    @NonNull
    public static FileListData ofError(@NonNull Path path, @NonNull Exception exception) {
        return new FileListData(State.ERROR, path, null, exception);
    }

    @NonNull
    public static FileListData ofSuccess(@NonNull Path path, @NonNull List<FileItem> fileList) {
        return new FileListData(State.SUCCESS, path, fileList, null);
    }
}
