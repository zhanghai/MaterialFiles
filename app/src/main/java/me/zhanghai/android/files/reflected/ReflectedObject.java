/*
 * Copyright (c) 2019 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

abstract class ReflectedObject<T> {

    @Nullable
    private T mObject;
    @NonNull
    private final Object mObjectLock = new Object();

    @NonNull
    public T get() throws ReflectedException {
        synchronized (mObjectLock) {
            if (mObject == null) {
                mObject = onGet();
            }
            return mObject;
        }
    }

    @NonNull
    protected abstract T onGet() throws ReflectedException;
}
