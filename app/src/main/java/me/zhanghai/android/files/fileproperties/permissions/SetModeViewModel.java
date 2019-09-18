/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import java.util.EnumSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;

public class SetModeViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<Set<PosixFileModeBit>> mModeLiveData;

    public SetModeViewModel(@NonNull Set<PosixFileModeBit> mode) {
        mModeLiveData = new MutableLiveData<>(mode);
    }

    @NonNull
    public LiveData<Set<PosixFileModeBit>> getModeLiveData() {
        return mModeLiveData;
    }

    @NonNull
    public Set<PosixFileModeBit> getMode() {
        return mModeLiveData.getValue();
    }

    public void toggleModeBit(@NonNull PosixFileModeBit modeBit) {
        Set<PosixFileModeBit> mode = mModeLiveData.getValue();
        mode = EnumSet.copyOf(mode);
        if (mode.contains(modeBit)) {
            mode.remove(modeBit);
        } else {
            mode.add(modeBit);
        }
        mModeLiveData.setValue(mode);
    }

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        private final Set<PosixFileModeBit> mMode;

        public Factory(@NonNull Set<PosixFileModeBit> mode) {
            mMode = mode;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new SetModeViewModel(mMode);
        }
    }
}
