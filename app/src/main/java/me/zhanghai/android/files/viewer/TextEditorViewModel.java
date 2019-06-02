/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import java.util.Objects;

import androidx.annotation.NonNull;
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

    public void setPath(@NonNull Path path) {
        if (!Objects.equals(mPathLiveData.getValue(), path)) {
            mPathLiveData.setValue(path);
        }
    }

    @NonNull
    public LiveData<FileContentData> getFileContentLiveData() {
        return mFileContentLiveData;
    }
}
