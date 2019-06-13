/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class WriteFileStateLiveData extends LiveData<WriteFileStateLiveData.State> {

    public enum State {
        READY,
        WRITING,
        ERROR,
        SUCCESS
    }

    private Exception mException;

    public WriteFileStateLiveData() {
        setValue(State.READY);
    }

    @SuppressLint("StaticFieldLeak")
    public void write(@NonNull Path path, @NonNull byte[] content) {
        if (getValue() != State.READY) {
            throw new IllegalStateException(getValue().toString());
        }
        setValue(State.WRITING);
        new AsyncTask<Void, Void, Exception>() {
            @Nullable
            @Override
            @WorkerThread
            protected Exception doInBackground(Void... parameters) {
                try (OutputStream outputStream = Files.newOutputStream(path,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE)) {
                    MoreFiles.copy(new ByteArrayInputStream(content), outputStream, null, 0);
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }
            @MainThread
            @Override
            protected void onPostExecute(@Nullable Exception e) {
                if (e == null) {
                    setValue(State.SUCCESS);
                } else {
                    setValue(State.ERROR);
                    mException = e;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public Exception getException() {
        return mException;
    }

    public void reset() {
        switch (getValue()) {
            case READY:
                return;
            case WRITING:
                throw new IllegalStateException();
            case ERROR:
            case SUCCESS:
                setValue(State.READY);
                mException = null;
        }
    }
}
