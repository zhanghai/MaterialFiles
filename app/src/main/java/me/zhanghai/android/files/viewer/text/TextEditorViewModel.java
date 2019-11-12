/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import java8.nio.file.Path;

public class TextEditorViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<Path> mPathLiveData = new MutableLiveData<>();

    @NonNull
    private final LiveData<FileContentData> mFileContentLiveData = Transformations.switchMap(
            mPathLiveData, FileContentLiveData::new);

    private final MutableLiveData<Boolean> mTextChangedLiveData = new MutableLiveData<>(false);

    private final WriteFileStateLiveData mWriteFileStateLiveData = new WriteFileStateLiveData();

    @Nullable
    private Parcelable mEditTextSavedState;

    public void setPath(@NonNull Path path) {
        if (!Objects.equals(mPathLiveData.getValue(), path)) {
            mPathLiveData.setValue(path);
        }
    }

    public void reload() {
        mPathLiveData.setValue(mPathLiveData.getValue());
    }

    @NonNull
    public LiveData<FileContentData> getFileContentLiveData() {
        return mFileContentLiveData;
    }

    @NonNull
    public FileContentData getFileContentData() {
        return mFileContentLiveData.getValue();
    }

    @NonNull
    public LiveData<Boolean> getTextChangedLiveData() {
        return mTextChangedLiveData;
    }

    public boolean isTextChanged() {
        return mTextChangedLiveData.getValue();
    }

    public void setTextChanged(boolean changed) {
        if (mFileContentLiveData.getValue().state != FileContentData.State.SUCCESS) {
            // Might happen if the animation is running and user is quick enough.
            return;
        }
        mTextChangedLiveData.setValue(changed);
    }

    @NonNull
    public WriteFileStateLiveData getWriteFileStateLiveData() {
        return mWriteFileStateLiveData;
    }

    public void setEditTextSavedState(@Nullable Parcelable editTextSavedState) {
        mEditTextSavedState = editTextSavedState;
    }

    @Nullable
    public Parcelable removeEditTextSavedState() {
        Parcelable savedState = mEditTextSavedState;
        mEditTextSavedState = null;
        return savedState;
    }
}
