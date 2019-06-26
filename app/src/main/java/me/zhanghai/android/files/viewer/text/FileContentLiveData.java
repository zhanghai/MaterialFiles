/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.os.AsyncTask;

import java.io.IOException;

import androidx.annotation.NonNull;
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

    private void loadValue() {
        setValue(FileContentData.ofLoading(mPath));
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            FileContentData value;
            try {
                if (Files.size(mPath) > MAX_SIZE) {
                    throw new IOException("File is too large");
                }
                byte[] content = MoreFiles.readAllBytes(mPath);
                value = FileContentData.ofSuccess(mPath, content);
            } catch (Exception e) {
                value = FileContentData.ofError(mPath, e);
            }
            postValue(value);
        });
    }
}
