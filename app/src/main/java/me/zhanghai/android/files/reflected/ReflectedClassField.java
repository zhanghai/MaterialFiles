/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedClassField<T> extends BaseReflectedField<T> {

    @NonNull
    private final ReflectedClass<T> mOwnerClass;

    public ReflectedClassField(@NonNull ReflectedClass<T> ownerClass, @NonNull String fieldName) {
        super(fieldName);

        mOwnerClass = ownerClass;
    }

    public ReflectedClassField(@NonNull String ownerClassName, @NonNull String fieldName) {
        this(new ReflectedClass<>(ownerClassName), fieldName);
    }

    @NonNull
    @Override
    protected Class<T> getOwnerClass() {
        return mOwnerClass.get();
    }
}
