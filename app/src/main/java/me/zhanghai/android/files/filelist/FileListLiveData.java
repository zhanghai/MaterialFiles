/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.Path;
import me.zhanghai.android.files.filesystem.JavaFileObserver;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.throwing.ThrowingFunction;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class FileListLiveData extends LiveData<FileListData> {

    @NonNull
    private final Path mPath;

    @Nullable
    private JavaFileObserver mFileObserver;

    public FileListLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
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
            @Override
            protected void onPostExecute(FileListData fileListData) {
                setValue(fileListData);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onActive() {
        if (LinuxFileSystemProvider.isLinuxPath(mPath)) {
            mFileObserver = new JavaFileObserver(mPath.toFile().getPath(), this::loadValue);
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
