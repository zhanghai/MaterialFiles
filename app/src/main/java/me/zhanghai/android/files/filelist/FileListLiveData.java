/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.filesystem.JavaFileObserver;
import me.zhanghai.android.files.filesystem.LocalFile;

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
