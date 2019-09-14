/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.filelist.PathObserver;
import me.zhanghai.android.files.util.CloseableLiveData;

public class FileLiveData extends CloseableLiveData<FileData> {

    @NonNull
    private final Path mPath;

    @NonNull
    private final PathObserver mObserver;

    private volatile boolean mChangedWhileInactive;

    public FileLiveData(@NonNull Path path) {
        this(path, null);
    }

    public FileLiveData(@NonNull FileItem file) {
        this(file.getPath(), file);
    }

    private FileLiveData(@NonNull Path path, @Nullable FileItem file) {
        mPath = path;
        if (file != null) {
            setValue(FileData.ofSuccess(file));
        } else {
            loadValue();
        }
        mObserver = new PathObserver(path, this::onChangeObserved);
    }

    public void loadValue() {
        setValue(FileData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            FileData value;
            try {
                FileItem file = FileItem.load(mPath);
                value = FileData.ofSuccess(file);
            } catch (Exception e) {
                value = FileData.ofError(e);
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
