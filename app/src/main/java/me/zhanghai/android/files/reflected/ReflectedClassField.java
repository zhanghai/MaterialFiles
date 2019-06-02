/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedClassField extends BaseReflectedField {

    @NonNull
    private final ReflectedClass mOwnerClass;

    public ReflectedClassField(@NonNull ReflectedClass ownerClass, @NonNull String fieldName) {
        super(fieldName);

        mOwnerClass = ownerClass;
    }

    public ReflectedClassField(@NonNull String ownerClassName, @NonNull String fieldName) {
        this(new ReflectedClass(ownerClassName), fieldName);
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass.get();
    }
}
