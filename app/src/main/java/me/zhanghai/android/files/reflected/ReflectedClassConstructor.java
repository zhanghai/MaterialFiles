/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedClassConstructor extends BaseReflectedConstructor {

    @NonNull
    private final ReflectedClass mOwnerClass;

    public ReflectedClassConstructor(@NonNull ReflectedClass ownerClass,
                                     @NonNull Object... parameterTypes) {
        super(parameterTypes);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass.get();
    }
}
