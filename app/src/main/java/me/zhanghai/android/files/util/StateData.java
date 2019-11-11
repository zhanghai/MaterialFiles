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

    private StateData(@NonNull State state, @Nullable Exception exception) {
        this.state = state;
        this.exception = exception;
    }

    @NonNull
    public static StateData ofReady() {
        return new StateData(State.READY, null);
    }

    @NonNull
    public static StateData ofLoading() {
        return new StateData(State.LOADING, null);
    }

    @NonNull
    public static StateData ofError(@Nullable Exception exception) {
        return new StateData(State.ERROR, exception);
    }

    @NonNull
    public static StateData ofSuccess() {
        return new StateData(State.SUCCESS, null);
    }
}
