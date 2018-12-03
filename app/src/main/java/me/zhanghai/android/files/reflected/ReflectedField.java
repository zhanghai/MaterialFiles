/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

public class ReflectedField extends BaseReflectedField {

    @NonNull
    private final Class<?> mOwnerClass;

    public ReflectedField(@NonNull Class<?> ownerClass, @NonNull String fieldName) {
        super(fieldName);

        mOwnerClass = ownerClass;
    }

    @NonNull
    @Override
    protected Class<?> getOwnerClass() {
        return mOwnerClass;
    }
}
