/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.io.Closeable;

import androidx.lifecycle.LiveData;

public abstract class CloseableLiveData<T> extends LiveData<T> implements Closeable {

    public CloseableLiveData(T value) {
        super(value);
    }

    public CloseableLiveData() {}

    @Override
    public abstract void close();
}
