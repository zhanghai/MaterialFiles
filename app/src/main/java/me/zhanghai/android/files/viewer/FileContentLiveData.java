/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class FileContentLiveData extends LiveData<FileContentData> {

    private static final long MAX_SIZE = 1024 * 1024;

    @NonNull
    private final Path mPath;

    public FileContentLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadValue() {
        setValue(FileContentData.ofLoading(mPath));
        new AsyncTask<Void, Void, FileContentData>() {
            @NonNull
            @Override
            @WorkerThread
            protected FileContentData doInBackground(Void... parameters) {
                try {
                    if (Files.size(mPath) > MAX_SIZE) {
                        throw new IOException("File is too large");
                    }
                    byte[] content = MoreFiles.readAllBytes(mPath);
                    return FileContentData.ofSuccess(mPath, content);
                } catch (Exception e) {
                    return FileContentData.ofError(mPath, e);
                }
            }
            @MainThread
            @Override
            protected void onPostExecute(FileContentData fileContentData) {
                setValue(fileContentData);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
