/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedMethod<T> extends BaseReflectedMethod<T> {

    @NonNull
    private final Class<T> mOwnerClass;

    public ReflectedMethod(@NonNull Class<T> ownerClass, @NonNull String methodName,
                           @NonNull Object... parameterTypes) {
        super(methodName, parameterTypes);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<T> getOwnerClass() {
        return mOwnerClass;
    }
}
