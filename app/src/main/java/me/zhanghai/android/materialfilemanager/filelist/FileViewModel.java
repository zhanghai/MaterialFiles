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

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;

public class FileViewModel extends ViewModel {

    private PathHistory mPathHistory = new PathHistory();
    private MutableLiveData<Uri> mPathData = new MutableLiveData<>();
    private LiveData<File> mFileData = Transformations.switchMap(mPathData, FileLiveData::new);

    public void pushPath(List<File> path) {
        mPathHistory.push(path);
        mPathData.setValue(getPathFile().getPath());
    }

    public boolean popPath() {
        boolean changed = mPathHistory.pop();
        if (changed) {
            mPathData.setValue(getPathFile().getPath());
        }
        return changed;
    }

    public List<File> getTrail() {
        return mPathHistory.getTrail();
    }

    public int getTrailIndex() {
        return mPathHistory.getTrailIndex();
    }

    public File getPathFile() {
        return mPathHistory.getCurrentFile();
    }

    public LiveData<File> getFileData() {
        return mFileData;
    }
}
