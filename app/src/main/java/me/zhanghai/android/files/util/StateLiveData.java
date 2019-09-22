/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.lifecycle.LiveData;

public abstract class StateLiveData extends LiveData<StateData> {

    public StateLiveData() {
        setValue(StateData.ofReady());
    }

    protected void checkReady() {
        StateData.State state = getValue().state;
        if (state != StateData.State.READY) {
            throw new IllegalStateException(state.toString());
        }
    }

    public void reset() {
        StateData.State state = getValue().state;
        switch (state) {
            case READY:
                return;
            case LOADING:
                throw new IllegalStateException(state.toString());
            case ERROR:
            case SUCCESS:
                setValue(StateData.ofReady());
                break;
        }
    }
}
