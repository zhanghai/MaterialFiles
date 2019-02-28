/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.annotation.Nullable;

public class Holder<T> {

    @Nullable
    public T value;

    public Holder() {}

    public Holder(@Nullable T value) {
        this.value = value;
    }
}
