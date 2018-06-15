/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.Files;

public class FileListLiveData extends LiveData<FileListData> {

    private Uri mPath;

    public FileListLiveData(Uri path) {
        mPath = path;
        loadData();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        new AsyncTask<Void, Void, FileListData>() {
            @Override
            protected FileListData doInBackground(Void... strings) {
                File file = Files.create(mPath);
                List<File> fileList = file.loadFileList();
                return FileListData.ofSuccess(file, fileList);
            }
            @Override
            protected void onPostExecute(FileListData fileListData) {
                setValue(fileListData);
            }
        }.execute();
    }
}
