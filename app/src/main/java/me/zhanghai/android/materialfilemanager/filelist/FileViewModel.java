/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.JavaLocalFile;
import me.zhanghai.android.materialfilemanager.settings.Settings;

public class FileViewModel extends ViewModel {

    private PathHistory mPathHistory = new PathHistory();
    private MutableLiveData<Uri> mPathData = new MutableLiveData<>();
    private MutableLiveData<FileSortOptions> mSortOptionsData = new MutableLiveData<>();
    private MediatorLiveData<File> mFileData = new FileMediatorLiveData(mPathData,
            mSortOptionsData);

    public FileViewModel() {
        // TODO
        File file = new JavaLocalFile(Uri.fromFile(new java.io.File(
                "/storage/emulated/0/Download")));
        pushPath(file.makeFilePath());
        mSortOptionsData.setValue(new FileSortOptions(Settings.FILE_LIST_SORT_BY.getEnumValue(),
                Settings.FILE_LIST_SORT_ORDER.getEnumValue(),
                Settings.FILE_LIST_SORT_DIRECTORIES_FIRST.getValue()));
    }

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

    private static class FileMediatorLiveData extends MediatorLiveData<File> {

        private LiveData<Uri> mPathData;
        private LiveData<FileSortOptions> mSortOptionsData;

        public FileMediatorLiveData(LiveData<Uri> pathData,
                                    LiveData<FileSortOptions> sortOptionsData) {

            mPathData = pathData;
            mSortOptionsData = sortOptionsData;

            addSource(mPathData, new Observer<Uri>() {
                LiveData<File> mSource;
                @Override
                public void onChanged(@Nullable Uri path) {
                    LiveData<File> newSource = new FileLiveData(path);
                    if (mSource == newSource) {
                        return;
                    }
                    if (mSource != null) {
                        removeSource(mSource);
                    }
                    mSource = newSource;
                    addSource(mSource, file -> {
                        sortFileList(file);
                        setValue(file);
                    });
                }
            });

            addSource(mSortOptionsData, fileSortOptions -> {
                File file = getValue();
                if (file != null) {
                    sortFileList(file);
                    setValue(file);
                }
            });
        }

        private void sortFileList(File file) {
            if (file == null) {
                return;
            }
            FileSortOptions sortOptions = mSortOptionsData.getValue();
            if (sortOptions != null) {
                sortOptions.sort(file.getFileList());
            }
        }
    }
}
