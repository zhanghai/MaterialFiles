/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.os.AsyncTask;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.provider.common.PosixFileStore;

class RemountStateLiveData extends LiveData<RemountStateData> {

    public RemountStateLiveData() {
        setValue(RemountStateData.ofReady());
    }

    public void remount(@NonNull PosixFileStore fileStore) {
        if (getValue().state != RemountStateData.State.READY) {
            throw new IllegalStateException(getValue().toString());
        }
        setValue(RemountStateData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                fileStore.setReadOnly(false);
            } catch (IOException e) {
                postValue(RemountStateData.ofError(e));
                return;
            }
            postValue(RemountStateData.ofSuccess());
        });
    }

    public void reset() {
        switch (getValue().state) {
            case READY:
                return;
            case LOADING:
                throw new IllegalStateException();
            case ERROR:
            case SUCCESS:
                setValue(RemountStateData.ofReady());
                break;
        }
    }
}
