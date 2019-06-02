/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedClassMethod extends BaseReflectedMethod {

    @NonNull
    private final ReflectedClass mOwnerClass;

    public ReflectedClassMethod(@NonNull ReflectedClass ownerClass, @NonNull String methodName,
                                @NonNull Object... parameterTypes) {
        super(methodName, parameterTypes);

        mOwnerClass = ownerClass;
    }

    public ReflectedClassMethod(@NonNull String ownerClassName, @NonNull String methodName,
                                @NonNull Object... parameterTypes) {
        this(new ReflectedClass(ownerClassName), methodName, parameterTypes);
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass.get();
    }
}
