/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import me.zhanghai.android.files.file.FileItem;

public class FilePropertiesViewModel extends ViewModel {

    @NonNull
    private final FileLiveData mFileLiveData;

    public FilePropertiesViewModel(@NonNull FileItem file) {
        mFileLiveData = new FileLiveData(file);
    }

    public void reloadFile() {
        mFileLiveData.loadValue();
    }

    @NonNull
    public LiveData<FileData> getFileLiveData() {
        return mFileLiveData;
    }

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        private final FileItem mFile;

        public Factory(@NonNull FileItem file) {
            mFile = file;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new FilePropertiesViewModel(mFile);
        }
    }
}
