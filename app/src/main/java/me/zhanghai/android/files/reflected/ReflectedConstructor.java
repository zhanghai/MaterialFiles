/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedConstructor extends BaseReflectedConstructor {

    @NonNull
    private final Class<?> mOwnerClass;

    public ReflectedConstructor(@NonNull Class<?> ownerClass, @NonNull Object... parameterTypes) {
        super(parameterTypes);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass;
    }
}
