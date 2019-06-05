/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.java.functional.Functional;
import me.zhanghai.java.functional.FunctionalException;
import me.zhanghai.java.functional.throwing.ThrowingFunction;

public class FileListLiveData extends LiveData<FileListData> implements Closeable {

    @NonNull
    private final Path mPath;

    @NonNull
    private final DirectoryObserver mObserver;

    private volatile boolean mChangedWhileInactive;

    @SuppressLint("StaticFieldLeak")
    public FileListLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
        mObserver = new DirectoryObserver(path, this::onChangeObserved);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadValue() {
        setValue(FileListData.ofLoading(mPath));
        new AsyncTask<Void, Void, FileListData>() {
            @Override
            @WorkerThread
            protected FileListData doInBackground(Void... parameters) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(mPath)) {
                    List<FileItem> fileList;
                    try {
                        fileList = Functional.map(directoryStream,
                                (ThrowingFunction<Path, FileItem>) FileItem::load);
                    } catch (FunctionalException e) {
                        if (e.getCause() instanceof DirectoryIteratorException) {
                            throw e.getCauseAs(DirectoryIteratorException.class).getCause();
                        }
                        throw e.getCauseAs(IOException.class);
                    }
                    return FileListData.ofSuccess(mPath, fileList);
                } catch (Exception e) {
                    return FileListData.ofError(mPath, e);
                }
            }
            @MainThread
            @Override
            protected void onPostExecute(FileListData fileListData) {
                setValue(fileListData);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onChangeObserved() {
        if (hasActiveObservers()) {
            loadValue();
        } else {
            mChangedWhileInactive = true;
        }
    }

    @Override
    protected void onActive() {
        if (mChangedWhileInactive) {
            loadValue();
            mChangedWhileInactive = false;
        }
    }

    @Override
    public void close() {
        mObserver.close();
    }
}
