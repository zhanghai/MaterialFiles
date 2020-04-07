/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import androidx.lifecycle.LiveData

abstract class StateLiveData : LiveData<StateData>() {
    protected fun checkReady() {
        val state = value!!.state
        check(state == StateData.State.READY) { state.toString() }
    }

    fun reset() {
        val state = value!!.state
        value = when (state) {
            StateData.State.READY -> return
            StateData.State.LOADING -> throw IllegalStateException(
                state.toString()
            )
            StateData.State.ERROR, StateData.State.SUCCESS -> StateData.ofReady()
        }
    }

    init {
        value = StateData.ofReady()
    }
}
