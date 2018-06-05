/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;

import me.zhanghai.android.materialfilemanager.filesystem.File;

public class FileViewModel extends ViewModel {

    private PathHistory mPathHistory = new PathHistory();
    private MutableLiveData<Uri> mPathData = new MutableLiveData<>();
    private LiveData<File> mFileData = Transformations.switchMap(mPathData, FileLiveData::new);

    public PathHistory getPathHistory() {
        return mPathHistory;
    }

    public void setPath(Uri path) {
        mPathData.setValue(path);
    }

    public LiveData<File> getFileData() {
        return mFileData;
    }
}
