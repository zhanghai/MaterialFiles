/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.util.CloseableLiveData;

public class SearchFileListLiveData extends CloseableLiveData<FileListData> {

    private static final long INTERVAL_MILLIS = 500;

    @NonNull
    private final Path mPath;
    @NonNull
    private final String mQuery;

    private Future<Void> mFuture;

    public SearchFileListLiveData(@NonNull Path path, @NonNull String query) {
        mPath = path;
        mQuery = query;
        loadValue();
    }

    private void loadValue() {
        setValue(FileListData.ofLoading(Collections.emptyList()));
        mFuture = ((ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR).submit(() -> {
            List<FileItem> fileList = new ArrayList<>();
            try {
                MoreFiles.search(mPath, mQuery, paths -> {
                    for (Path path : paths) {
                        FileItem fileItem;
                        try {
                            fileItem = FileItem.load(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                            // TODO: Support file without information.
                            continue;
                        }
                        fileList.add(fileItem);
                    }
                    postValue(FileListData.ofLoading(new ArrayList<>(fileList)));
                }, INTERVAL_MILLIS);
                postValue(FileListData.ofSuccess(fileList));
            } catch (Exception e) {
                postValue(FileListData.ofError(e));
            }
            return null;
        });
    }

    @Override
    public void close() {
        mFuture.cancel(true);
    }
}
