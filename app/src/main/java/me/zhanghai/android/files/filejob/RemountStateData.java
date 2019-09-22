/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.util.StateData;

public class RemountStateData extends StateData {

    private RemountStateData(@NonNull State state, @Nullable Exception exception) {
        super(state, exception);
    }

    @NonNull
    public static RemountStateData ofReady() {
        return new RemountStateData(State.READY, null);
    }

    @NonNull
    public static RemountStateData ofLoading() {
        return new RemountStateData(State.LOADING, null);
    }

    @NonNull
    public static RemountStateData ofError(@NonNull Exception exception) {
        return new RemountStateData(State.ERROR, exception);
    }

    @NonNull
    public static RemountStateData ofSuccess() {
        return new RemountStateData(State.SUCCESS, null);
    }
}
