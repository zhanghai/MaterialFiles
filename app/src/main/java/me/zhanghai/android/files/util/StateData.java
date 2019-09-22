/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StateData {

    public enum State {
        READY,
        LOADING,
        ERROR,
        SUCCESS
    }

    @NonNull
    public final State state;
    @Nullable
    public final Exception exception;

    protected StateData(@NonNull State state, @Nullable Exception exception) {
        this.state = state;
        this.exception = exception;
    }
}
