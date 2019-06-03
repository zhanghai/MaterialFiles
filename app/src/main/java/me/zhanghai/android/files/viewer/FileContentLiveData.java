/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.InputStream;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.IoUtils;

public class FileContentLiveData extends LiveData<FileContentData> {

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
                // TODO: Just use Files.readAllBytes(), if all our providers support
                //  newByteChannel()?
                try {
                    long sizeLong = Files.size(mPath);
                    if (sizeLong > Integer.MAX_VALUE) {
                        throw new OutOfMemoryError("size " + sizeLong);
                    }
                    int size = (int) sizeLong;
                    byte[] content;
                    try (InputStream inputStream = Files.newInputStream(mPath)) {
                        content = IoUtils.inputStreamToByteArray(inputStream, size);
                    }
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
