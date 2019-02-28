/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcelable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;

public class FileListViewModel extends ViewModel {

    @NonNull
    private final TrailLiveData mTrailLiveData = new TrailLiveData();
    @NonNull
    private final LiveData<Path> mCurrentPathLiveData = Transformations.map(mTrailLiveData,
            TrailData::getCurrentPath);
    @NonNull
    private final LiveData<FileListData> mFileListLiveData = Transformations.switchMap(
            mCurrentPathLiveData, FileListLiveData::new);
    @NonNull
    private final LiveData<BreadcrumbData> mBreadcrumbLiveData = new BreadcrumbLiveData(
            mTrailLiveData);
    @NonNull
    private final MutableLiveData<Set<FileItem>> mSelectedFilesLiveData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<FilePasteMode> mPasteModeLiveData = new MutableLiveData<>();

    public FileListViewModel() {
        mSelectedFilesLiveData.setValue(new HashSet<>());
        mPasteModeLiveData.setValue(FilePasteMode.NONE);
    }

    public boolean hasTrail() {
        return mTrailLiveData.getValue() != null;
    }

    public void navigateTo(@NonNull Parcelable lastState, @NonNull Path path) {
        mTrailLiveData.navigateTo(lastState, path);
    }

    public void resetTo(@NonNull Path path) {
        mTrailLiveData.resetTo(path);
    }

    public boolean navigateUp(boolean overrideBreadcrumb) {
        if (!overrideBreadcrumb && mBreadcrumbLiveData.getValue().selectedIndex == 0) {
            return false;
        }
        return mTrailLiveData.navigateUp();
    }

    @Nullable
    public Parcelable getPendingState() {
        return mTrailLiveData.getValue().getPendingState();
    }

    public void reload() {
        Path path = mTrailLiveData.getValue().getCurrentPath();
        if (ArchiveFileSystemProvider.isArchivePath(path)) {
            ArchiveFileSystemProvider.refresh(path);
        }
        mTrailLiveData.reload();
    }

    @NonNull
    public LiveData<Path> getCurrentPathLiveData() {
        return mCurrentPathLiveData;
    }

    @NonNull
    public Path getCurrentPath() {
        return mCurrentPathLiveData.getValue();
    }

    @NonNull
    public LiveData<FileListData> getFileListLiveData() {
        return mFileListLiveData;
    }

    @NonNull
    public FileListData getFileListData() {
        return mFileListLiveData.getValue();
    }

    @NonNull
    public LiveData<BreadcrumbData> getBreadcrumbLiveData() {
        return mBreadcrumbLiveData;
    }

    @NonNull
    public LiveData<Set<FileItem>> getSelectedFilesLiveData() {
        return mSelectedFilesLiveData;
    }

    @NonNull
    public Set<FileItem> getSelectedFiles() {
        return mSelectedFilesLiveData.getValue();
    }

    public void selectFile(@NonNull FileItem file, boolean selected) {
        selectFiles(Collections.singleton(file), selected);
    }

    public void selectFiles(@NonNull Set<FileItem> files, boolean selected) {
        Set<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles == files) {
            if (!selected && !selectedFiles.isEmpty()) {
                selectedFiles.clear();
                mSelectedFilesLiveData.setValue(selectedFiles);
            }
            return;
        }
        boolean changed = false;
        for (FileItem file : files) {
            changed |= selected ? selectedFiles.add(file) : selectedFiles.remove(file);
        }
        if (changed) {
            mSelectedFilesLiveData.setValue(selectedFiles);
        }
    }

    public void selectAllFiles() {
        List<FileItem> files = mFileListLiveData.getValue().fileList;
        if (files == null) {
            return;
        }
        selectFiles(new HashSet<>(files), true);
    }

    public void replaceSelectedFiles(@NonNull Set<FileItem> files) {
        Set<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.equals(files)) {
            return;
        }
        selectedFiles.clear();
        selectedFiles.addAll(files);
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    public void clearSelectedFiles() {
        Set<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.isEmpty()) {
            return;
        }
        selectedFiles.clear();
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    @NonNull
    public LiveData<FilePasteMode> getPasteModeLiveData() {
        return mPasteModeLiveData;
    }

    @NonNull
    public FilePasteMode getPasteMode() {
        return mPasteModeLiveData.getValue();
    }

    public void setPasteMode(@NonNull FilePasteMode pasteMode) {
        mPasteModeLiveData.setValue(pasteMode);
    }
}
