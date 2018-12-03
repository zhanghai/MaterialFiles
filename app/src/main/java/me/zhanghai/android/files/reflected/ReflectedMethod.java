/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedMethod extends BaseReflectedMethod {

    @NonNull
    private final Class<?> mOwnerClass;

    public ReflectedMethod(@NonNull Class<?> ownerClass, @NonNull String methodName,
                           @NonNull Object... parameterTypes) {
        super(methodName, parameterTypes);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass;
    }
}
