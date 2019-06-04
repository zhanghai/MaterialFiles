/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ReflectedClass<T> extends ReflectedObject<Class<T>> {

    @NonNull
    private final String mClassName;

    public ReflectedClass(@NonNull String className) {
        mClassName = Objects.requireNonNull(className);
    }

    @NonNull
    @Override
    protected Class<T> onGet() throws ReflectedException {
        return ReflectedAccessor.getClass(mClassName);
    }
}
