/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.os.AsyncTask;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileFactory;
import me.zhanghai.android.materialfilemanager.filesystem.JavaLocalFile;

public class FileLiveData extends LiveData<File> {

    private Uri mPath;

    public FileLiveData(Uri path) {
        mPath = path;
        loadData();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... strings) {
                File file = FileFactory.create(mPath);
                file.loadFileList();
                return file;
            }
            @Override
            protected void onPostExecute(File file) {
                setValue(file);
            }
        }.execute();
    }
}
