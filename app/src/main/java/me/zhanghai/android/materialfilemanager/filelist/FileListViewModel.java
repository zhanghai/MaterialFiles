/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.Files;
import me.zhanghai.android.materialfilemanager.settings.Settings;

public class FileListViewModel extends ViewModel {

    private TrailLiveData mTrailLiveData = new TrailLiveData();
    private LiveData<File> mCurrentFileLiveData = Transformations.map(mTrailLiveData,
            TrailData::getCurrentFile);
    private LiveData<FileListData> mFileListLiveData = Transformations.switchMap(
            mCurrentFileLiveData, FileListLiveData::new);
    private MutableLiveData<FileSortOptions> mSortOptionsLiveData = new MutableLiveData<>();
    private LiveData<BreadcrumbData> mBreadcrumbLiveData = new BreadcrumbLiveData(mTrailLiveData);
    private MutableLiveData<Set<File>> mSelectedFilesLiveData = new MutableLiveData<>();
    private MutableLiveData<FilePasteMode> mPasteModeLiveData = new MutableLiveData<>();

    public FileListViewModel() {
        mSortOptionsLiveData.setValue(new FileSortOptions(Settings.FILE_LIST_SORT_BY.getEnumValue(),
                Settings.FILE_LIST_SORT_ORDER.getEnumValue(),
                Settings.FILE_LIST_SORT_DIRECTORIES_FIRST.getValue()));
        mSelectedFilesLiveData.setValue(new HashSet<>());
        mPasteModeLiveData.setValue(FilePasteMode.NONE);
        // FIXME: Handle multi instances.
        mTrailLiveData.observeForever(trailData -> Files.onTrailChanged(trailData.getTrail()));
    }

    public void navigateTo(Parcelable lastState, File file) {
        mTrailLiveData.navigateTo(lastState, file);
    }

    public void resetTo(File file) {
        mTrailLiveData.resetTo(file);
    }

    public boolean navigateUp(boolean overrideBreadcrumb) {
        if (!overrideBreadcrumb && mBreadcrumbLiveData.getValue().selectedIndex == 0) {
            return false;
        }
        return mTrailLiveData.navigateUp();
    }

    public Parcelable getPendingState() {
        return mTrailLiveData.getValue().getPendingState();
    }

    public void reload() {
        Files.invalidateCache(mTrailLiveData.getValue().getCurrentFile());
        mTrailLiveData.reload();
    }

    public LiveData<File> getCurrentFileLiveData() {
        return mCurrentFileLiveData;
    }

    public File getCurrentFile() {
        return mCurrentFileLiveData.getValue();
    }

    public LiveData<FileListData> getFileListLiveData() {
        return mFileListLiveData;
    }

    public LiveData<FileSortOptions> getSortOptionsLiveData() {
        return mSortOptionsLiveData;
    }

    public FileSortOptions getSortOptions() {
        return mSortOptionsLiveData.getValue();
    }

    public void setSortOptions(FileSortOptions sortOptions) {
        Settings.FILE_LIST_SORT_BY.putEnumValue(sortOptions.getBy());
        Settings.FILE_LIST_SORT_ORDER.putEnumValue(sortOptions.getOrder());
        Settings.FILE_LIST_SORT_DIRECTORIES_FIRST.putValue(sortOptions.isDirectoriesFirst());
        mSortOptionsLiveData.setValue(sortOptions);
    }

    public LiveData<BreadcrumbData> getBreadcrumbLiveData() {
        return mBreadcrumbLiveData;
    }

    public LiveData<Set<File>> getSelectedFilesLiveData() {
        return mSelectedFilesLiveData;
    }

    public Set<File> getSelectedFiles() {
        return mSelectedFilesLiveData.getValue();
    }

    public void selectFile(File file, boolean selected) {
        selectFiles(Collections.singleton(file), selected);
    }

    public void selectFiles(Set<File> files, boolean selected) {
        Set<File> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles == files) {
            if (!selected && !selectedFiles.isEmpty()) {
                selectedFiles.clear();
                mSelectedFilesLiveData.setValue(selectedFiles);
            }
            return;
        }
        boolean changed = false;
        for (File file : files) {
            changed |= selected ? selectedFiles.add(file) : selectedFiles.remove(file);
        }
        if (changed) {
            mSelectedFilesLiveData.setValue(selectedFiles);
        }
    }

    public void selectAllFiles() {
        List<File> fileList = mFileListLiveData.getValue().fileList;
        if (fileList == null) {
            return;
        }
        selectFiles(new HashSet<>(fileList), true);
    }

    public void setSelectedFiles(Set<File> files) {
        Set<File> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.equals(files)) {
            return;
        }
        selectedFiles.clear();
        selectedFiles.addAll(files);
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    public void clearSelectedFiles() {
        Set<File> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.isEmpty()) {
            return;
        }
        selectedFiles.clear();
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    public LiveData<FilePasteMode> getPasteModeLiveData() {
        return mPasteModeLiveData;
    }

    public FilePasteMode getPasteMode() {
        return mPasteModeLiveData.getValue();
    }

    public void setPasteMode(FilePasteMode pasteMode) {
        mPasteModeLiveData.setValue(pasteMode);
    }
}
