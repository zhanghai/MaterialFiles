/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.util.StateData;
import me.zhanghai.android.files.util.StateLiveData;

public class WriteFileStateLiveData extends StateLiveData {

    public void write(@NonNull Path path, @NonNull byte[] content) {
        checkReady();
        setValue(StateData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try (OutputStream outputStream = MoreFiles.newOutputStream(path,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE)) {
                MoreFiles.copy(new ByteArrayInputStream(content), outputStream, null, 0);
            } catch (Exception e) {
                postValue(StateData.ofError(e));
                return;
            }
            postValue(StateData.ofSuccess());
        });
    }
}
