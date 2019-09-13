/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcelable;

import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.util.CloseableLiveData;
import me.zhanghai.android.files.util.CollectionUtils;

// TODO: Use SavedStateHandle to save state.
public class FileListViewModel extends ViewModel {

    @NonNull
    private final TrailLiveData mTrailLiveData = new TrailLiveData();
    @NonNull
    private final LiveData<Path> mCurrentPathLiveData = Transformations.map(mTrailLiveData,
            TrailData::getCurrentPath);
    @NonNull
    private final MutableLiveData<SearchState> mSearchStateLiveData = new MutableLiveData<>(
            new SearchState(false, ""));
    @NonNull
    private final FileListSwitchMapLiveData mFileListLiveData = new FileListSwitchMapLiveData(
            mCurrentPathLiveData, mSearchStateLiveData);
    @NonNull
    private final MutableLiveData<Boolean> mSearchViewExpandedLiveData = new MutableLiveData<>(
            false);
    @NonNull
    private final MutableLiveData<String> mSearchViewQueryLiveData = new MutableLiveData<>("");
    @NonNull
    private final LiveData<BreadcrumbData> mBreadcrumbLiveData = new BreadcrumbLiveData(
            mTrailLiveData);
    @NonNull
    private final FileSortOptionsLiveData mSortOptionsLiveData = new FileSortOptionsLiveData(
            mCurrentPathLiveData);
    @NonNull
    private final FileSortPathSpecificLiveData mSortPathSpecificLiveData =
            new FileSortPathSpecificLiveData(mCurrentPathLiveData);
    @NonNull
    private final MutableLiveData<PickOptions> mPickOptionsLiveData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<LinkedHashSet<FileItem>> mSelectedFilesLiveData =
            new MutableLiveData<>(new LinkedHashSet<>());
    @NonNull
    private static final MutableLiveData<PasteState> sPasteStateLiveData = new MutableLiveData<>(
            new PasteState());

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
    public LiveData<SearchState> getSearchStateLiveData() {
        return mSearchStateLiveData;
    }

    @NonNull
    public SearchState getSearchState() {
        return mSearchStateLiveData.getValue();
    }

    public void search(@NonNull String query) {
        SearchState searchState = mSearchStateLiveData.getValue();
        if (searchState.searching && Objects.equals(searchState.query, query)) {
            return;
        }
        mSearchStateLiveData.setValue(new SearchState(true, query));
    }

    public void stopSearching() {
        SearchState searchState = mSearchStateLiveData.getValue();
        if (!searchState.searching) {
            return;
        }
        mSearchStateLiveData.setValue(new SearchState(false, ""));
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
    public MutableLiveData<Boolean> getSearchViewExpandedLiveData() {
        return mSearchViewExpandedLiveData;
    }

    public boolean isSearchViewExpanded() {
        return mSearchViewExpandedLiveData.getValue();
    }

    public void setSearchViewExpanded(boolean expanded) {
        if (mSearchViewExpandedLiveData.getValue() == expanded) {
            return;
        }
        mSearchViewExpandedLiveData.setValue(expanded);
    }

    @NonNull
    public String getSearchViewQuery() {
        return mSearchViewQueryLiveData.getValue();
    }

    public void setSearchViewQuery(@NonNull String query) {
        if (Objects.equals(mSearchViewQueryLiveData.getValue(), query)) {
            return;
        }
        mSearchViewQueryLiveData.setValue(query);
    }

    @NonNull
    public LiveData<BreadcrumbData> getBreadcrumbLiveData() {
        return mBreadcrumbLiveData;
    }

    @NonNull
    public LiveData<FileSortOptions> getSortOptionsLiveData() {
        return mSortOptionsLiveData;
    }

    @NonNull
    public FileSortOptions getSortOptions() {
        return mSortOptionsLiveData.getValue();
    }

    public void setSortBy(@NonNull FileSortOptions.By by) {
        mSortOptionsLiveData.putBy(by);
    }

    public void setSortOrder(@NonNull FileSortOptions.Order order) {
        mSortOptionsLiveData.putOrder(order);
    }

    public void setSortDirectoriesFirst(boolean directoriesFirst) {
        mSortOptionsLiveData.putDirectoriesFirst(directoriesFirst);
    }

    @NonNull
    public LiveData<Boolean> getSortPathSpecificLiveData() {
        return mSortPathSpecificLiveData;
    }

    public boolean isSortPathSpecific() {
        return mSortPathSpecificLiveData.getValue();
    }

    public void setSortPathSpecific(boolean pathSpecific) {
        mSortPathSpecificLiveData.putValue(pathSpecific);
    }

    @NonNull
    public LiveData<PickOptions> getPickOptionsLiveData() {
        return mPickOptionsLiveData;
    }

    @Nullable
    public PickOptions getPickOptions() {
        return mPickOptionsLiveData.getValue();
    }

    public void setPickOptions(@NonNull PickOptions pickOptions) {
        mPickOptionsLiveData.setValue(pickOptions);
    }

    @NonNull
    public LiveData<LinkedHashSet<FileItem>> getSelectedFilesLiveData() {
        return mSelectedFilesLiveData;
    }

    @NonNull
    public LinkedHashSet<FileItem> getSelectedFiles() {
        return mSelectedFilesLiveData.getValue();
    }

    public void selectFile(@NonNull FileItem file, boolean selected) {
        selectFiles(CollectionUtils.singletonLinkedSet(file), selected);
    }

    public void selectFiles(@NonNull LinkedHashSet<FileItem> files, boolean selected) {
        LinkedHashSet<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
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

    public void replaceSelectedFiles(@NonNull LinkedHashSet<FileItem> files) {
        LinkedHashSet<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.equals(files)) {
            return;
        }
        selectedFiles.clear();
        selectedFiles.addAll(files);
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    public void clearSelectedFiles() {
        LinkedHashSet<FileItem> selectedFiles = mSelectedFilesLiveData.getValue();
        if (selectedFiles.isEmpty()) {
            return;
        }
        selectedFiles.clear();
        mSelectedFilesLiveData.setValue(selectedFiles);
    }

    @NonNull
    public LiveData<PasteState> getPasteStateLiveData() {
        return sPasteStateLiveData;
    }

    @NonNull
    public PasteState getPasteState() {
        return sPasteStateLiveData.getValue();
    }

    public void addToPasteState(boolean copy, @NonNull LinkedHashSet<FileItem> files) {
        PasteState pasteState = sPasteStateLiveData.getValue();
        boolean changed = false;
        if (pasteState.copy != copy) {
            changed = !pasteState.files.isEmpty();
            pasteState.files.clear();
            pasteState.copy = copy;
        }
        changed |= pasteState.files.addAll(files);
        if (changed) {
            sPasteStateLiveData.setValue(pasteState);
        }
    }

    public void clearPasteState() {
        PasteState pasteState = sPasteStateLiveData.getValue();
        if (pasteState.files.isEmpty()) {
            return;
        }
        pasteState.files.clear();
        sPasteStateLiveData.setValue(pasteState);
    }

    @Override
    protected void onCleared() {
        mFileListLiveData.close();
    }

    private static class FileListSwitchMapLiveData extends MediatorLiveData<FileListData>
            implements Closeable {

        @NonNull
        private LiveData<Path> mPathLiveData;
        @NonNull
        private LiveData<SearchState> mSearchStateLiveData;

        @Nullable
        private CloseableLiveData<FileListData> mLiveData;

        public FileListSwitchMapLiveData(@NonNull LiveData<Path> pathLiveData,
                                         @NonNull LiveData<SearchState> searchStateLiveData) {

            mPathLiveData = pathLiveData;
            mSearchStateLiveData = searchStateLiveData;

            addSource(mPathLiveData, path -> updateSource());
            addSource(mSearchStateLiveData, searchState -> updateSource());
        }

        private void updateSource() {
            Path path = mPathLiveData.getValue();
            SearchState searchState = mSearchStateLiveData.getValue();
            CloseableLiveData<FileListData> newLiveData = searchState.searching ?
                    new SearchFileListLiveData(path, searchState.query)
                    : new FileListLiveData(path);
            if (mLiveData != null) {
                removeSource(mLiveData);
                mLiveData.close();
            }
            mLiveData = newLiveData;
            addSource(mLiveData, FileListSwitchMapLiveData.this::setValue);
        }

        @Override
        public void close() {
            if (mLiveData != null) {
                removeSource(mLiveData);
                mLiveData.close();
                mLiveData = null;
            }
        }
    }
}
