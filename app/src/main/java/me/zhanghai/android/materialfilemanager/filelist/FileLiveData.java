/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.support.annotation.Nullable;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;

public class FileLiveData extends LiveData<File> {

    private Uri mPath;
    private FileObserver mObserver;
    private boolean mChanged;

    public FileLiveData(Uri path) {
        mPath = path;
        // TODO: Is file Uri?
        mObserver = new FileObserver(mPath.getPath(), FileObserver.MODIFY | FileObserver.ATTRIB
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO | FileObserver.CREATE
                | FileObserver.DELETE | FileObserver.DELETE_SELF | FileObserver.MOVE_SELF) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                if (hasActiveObservers()) {
                    loadData();
                } else {
                    mChanged = true;
                }
            }
        };
        loadData();
    }

    @Override
    protected void onActive() {
        if (mChanged) {
            loadData();
        }
        mObserver.startWatching();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        mChanged = false;
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... strings) {
                File file = new LocalFile(mPath);
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
