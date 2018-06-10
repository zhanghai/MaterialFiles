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
import android.os.Parcelable;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.JavaLocalFile;
import me.zhanghai.android.materialfilemanager.settings.Settings;

public class FileViewModel extends ViewModel {

    private PathHistory mPathHistory = new PathHistory();
    private MutableLiveData<Uri> mPathData = new MutableLiveData<>();
    private LiveData<File> mFileData = Transformations.switchMap(mPathData, FileLiveData::new);
    private MutableLiveData<FileSortOptions> mSortOptionsData = new MutableLiveData<>();

    public FileViewModel() {
        // TODO
        File file = new JavaLocalFile(Uri.fromFile(new java.io.File(
                "/storage/emulated/0/Download")));
        pushPath(null, file.makeFilePath());
        mSortOptionsData.setValue(new FileSortOptions(Settings.FILE_LIST_SORT_BY.getEnumValue(),
                Settings.FILE_LIST_SORT_ORDER.getEnumValue(),
                Settings.FILE_LIST_SORT_DIRECTORIES_FIRST.getValue()));
    }

    public void pushPath(Parcelable lastState, List<File> path) {
        mPathHistory.push(lastState, path);
        mPathData.setValue(getPathFile().getPath());
    }

    public boolean popPath() {
        boolean changed = mPathHistory.pop();
        if (changed) {
            mPathData.setValue(getPathFile().getPath());
        }
        return changed;
    }

    public Parcelable getPendingState() {
        return mPathHistory.getPendingState();
    }

    public void reload() {
        mPathData.setValue(mPathData.getValue());
    }

    public LiveData<File> getFileData() {
        return mFileData;
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

    public LiveData<FileSortOptions> getSortOptionsData() {
        return mSortOptionsData;
    }

    public FileSortOptions getSortOptions() {
        return mSortOptionsData.getValue();
    }

    public void setSortOptions(FileSortOptions sortOptions) {
        Settings.FILE_LIST_SORT_BY.putEnumValue(sortOptions.getBy());
        Settings.FILE_LIST_SORT_ORDER.putEnumValue(sortOptions.getOrder());
        Settings.FILE_LIST_SORT_DIRECTORIES_FIRST.putValue(sortOptions.isDirectoriesFirst());
        mSortOptionsData.setValue(sortOptions);
    }
}
