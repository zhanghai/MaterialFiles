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

    private MutableLiveData<Uri> mPath = new MutableLiveData<>();
    private LiveData<File> mFile = Transformations.switchMap(mPath, FileLiveData::new);

    public void setPath(Uri path) {
        mPath.setValue(path);
    }

    public LiveData<File> getData() {
        return mFile;
    }
}
