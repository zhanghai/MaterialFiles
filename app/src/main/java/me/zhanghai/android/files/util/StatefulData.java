/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StatefulData<T> {

    public enum State {
        LOADING,
        ERROR,
        SUCCESS
    }

    @NonNull
    public final State state;
    @Nullable
    public final T data;
    @Nullable
    public final Exception exception;

    protected StatefulData(@NonNull State state, @Nullable T data, @Nullable Exception exception) {
        this.state = state;
        this.data = data;
        this.exception = exception;
    }
}
