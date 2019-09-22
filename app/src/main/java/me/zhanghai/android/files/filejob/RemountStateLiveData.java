/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.os.AsyncTask;

import java.io.IOException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.util.StateData;
import me.zhanghai.android.files.util.StateLiveData;

class RemountStateLiveData extends StateLiveData {

    public void remount(@NonNull PosixFileStore fileStore) {
        checkReady();
        setValue(StateData.ofLoading());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                fileStore.setReadOnly(false);
            } catch (IOException e) {
                postValue(StateData.ofError(e));
                return;
            }
            postValue(StateData.ofSuccess());
        });
    }
}
