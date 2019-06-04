/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedField<T> extends BaseReflectedField<T> {

    @NonNull
    private final Class<T> mOwnerClass;

    public ReflectedField(@NonNull Class<T> ownerClass, @NonNull String fieldName) {
        super(fieldName);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<T> getOwnerClass() {
        return mOwnerClass;
    }
}
