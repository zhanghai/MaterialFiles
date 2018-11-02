/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.JavaFileObserver;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;

public class FileListLiveData extends LiveData<FileListData> {

    @NonNull
    private final File mFile;

    @Nullable
    private JavaFileObserver mFileObserver;

    public FileListLiveData(@NonNull File file) {
        mFile = file;
        loadValue();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadValue() {
        setValue(FileListData.ofLoading(mFile));
        new AsyncTask<Void, Void, FileListData>() {
            @Override
            @WorkerThread
            protected FileListData doInBackground(Void... parameters) {
                try {
                    List<File> fileList = mFile.getChildren();
                    return FileListData.ofSuccess(mFile, fileList);
                } catch (Exception e) {
                    return FileListData.ofError(mFile, e);
                }
            }
            @Override
            protected void onPostExecute(FileListData fileListData) {
                setValue(fileListData);
            }
        // FIXME: Actually not good as we only have one SU shell; But good for loading archives.
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onActive() {
        if (mFile instanceof LocalFile) {
            LocalFile file = (LocalFile) mFile;
            mFileObserver = new JavaFileObserver(file.getPath(), this::loadValue);
            mFileObserver.startWatching();
        }
    }

    @Override
    protected void onInactive() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }
}
