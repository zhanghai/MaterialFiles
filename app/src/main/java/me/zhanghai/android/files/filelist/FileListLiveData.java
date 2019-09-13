/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.util.CloseableLiveData;

public class FileListLiveData extends CloseableLiveData<FileListData> {

    @NonNull
    private final Path mPath;

    @NonNull
    private final PathObserver mObserver;

    private volatile boolean mChangedWhileInactive;

    public FileListLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
        mObserver = new PathObserver(path, this::onChangeObserved);
    }

    private void loadValue() {
        setValue(FileListData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            FileListData value;
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(mPath)) {
                List<FileItem> fileList = new ArrayList<>();
                for (Path path : directoryStream) {
                    try {
                        fileList.add(FileItem.load(path));
                    } catch (DirectoryIteratorException | IOException e) {
                        // TODO: Ignoring such a file can be misleading and we need to support files
                        //  without information.
                        e.printStackTrace();
                    }
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
