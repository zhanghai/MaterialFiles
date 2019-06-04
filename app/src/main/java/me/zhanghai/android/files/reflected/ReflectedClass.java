/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ReflectedClass<T> {

    @NonNull
    private final String mClassName;

    @Nullable
    private Class<T> mClass;
    @NonNull
    private final Object mClassLock = new Object();

    public ReflectedClass(@NonNull String className) {
        mClassName = className;
    }

    @NonNull
    public Class<T> get() throws ReflectedException {
        synchronized (mClassLock) {
            if (mClass == null) {
                //noinspection unchecked
                mClass = (Class<T>) ReflectedAccessor.getClass(mClassName);
            }
            return mClass;
        }
    }
}
