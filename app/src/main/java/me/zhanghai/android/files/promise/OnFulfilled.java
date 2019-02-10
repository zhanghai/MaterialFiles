/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.promise;

import androidx.annotation.Nullable;

@FunctionalInterface
public interface OnFulfilled<T, R> {

    @Nullable
    R onFulfilled(T value) throws Exception;
}
