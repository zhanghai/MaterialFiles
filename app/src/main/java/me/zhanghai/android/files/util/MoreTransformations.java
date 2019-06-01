/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.io.Closeable;
import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import me.zhanghai.android.files.functional.compat.Function;

public class MoreTransformations {

    @MainThread
    @NonNull
    public static <X, Y, LDY extends LiveData<Y> & Closeable> LiveData<Y> switchMapCloseable(
            @NonNull LiveData<X> source, @NonNull Function<X, LDY> switchMapFunction) {
        MediatorLiveData<Y> result = new MediatorLiveData<>();
        result.addSource(source, new Observer<X>() {
            private LDY mSource;
            @Override
            public void onChanged(@Nullable X x) {
                LDY newLiveData = switchMapFunction.apply(x);
                if (mSource == newLiveData) {
                    return;
                }
                if (mSource != null) {
                    result.removeSource(mSource);
                    try {
                        mSource.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mSource = newLiveData;
                if (mSource != null) {
                    result.addSource(mSource, result::setValue);
                }
            }
        });
        return result;
    }
}
