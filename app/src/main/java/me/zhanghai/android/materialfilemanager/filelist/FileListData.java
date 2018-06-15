/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;

public class FileListData {

    public enum State {
        LOADING,
        ERROR,
        SUCCESS
    }

    public State state;
    public File file;
    public List<File> fileList;
    public Exception exception;

    private FileListData(State state, File file, List<File> fileList, Exception exception) {
        this.state = state;
        this.file = file;
        this.fileList = fileList;
        this.exception = exception;
    }

    public static FileListData ofLoading(File file) {
        return new FileListData(State.LOADING, file, null, null);
    }

    public static FileListData ofError(File file, Exception exception) {
        return new FileListData(State.ERROR, file, null, exception);
    }

    public static FileListData ofSuccess(File file, List<File> fileList) {
        return new FileListData(State.SUCCESS, file, fileList, null);
    }
}
