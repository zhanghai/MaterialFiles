/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.java.functional.Functional;
import me.zhanghai.java.functional.FunctionalException;
import me.zhanghai.java.functional.throwing.ThrowingFunction;

public class FileListLiveData extends CloseableLiveData<FileListData> {

    @NonNull
    private final Path mPath;

    @NonNull
    private final DirectoryObserver mObserver;

    private volatile boolean mChangedWhileInactive;

    public FileListLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
        mObserver = new DirectoryObserver(path, this::onChangeObserved);
    }

    private void loadValue() {
        setValue(FileListData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            FileListData value;
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(mPath)) {
                List<FileItem> fileList;
                try {
                    fileList = Functional.map(directoryStream,
                            (ThrowingFunction<Path, FileItem>) FileItem::load);
                } catch (FunctionalException e) {
                    // TODO: Support file without information.
                    if (e.getCause() instanceof DirectoryIteratorException) {
                        throw e.getCauseAs(DirectoryIteratorException.class).getCause();
                    }
                    throw e.getCauseAs(IOException.class);
                }
                value = FileListData.ofSuccess(fileList);
            } catch (Exception e) {
                value = FileListData.ofError(e);
            }
            postValue(value);
        });
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
