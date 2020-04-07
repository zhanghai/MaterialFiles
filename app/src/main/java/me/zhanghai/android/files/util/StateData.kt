/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

class StateData private constructor(
    val state: State,
    val exception: Exception?
) {
    enum class State {
        READY, LOADING, ERROR, SUCCESS
    }

    companion object {
        fun ofReady(): StateData {
            return StateData(State.READY, null)
        }

        @kotlin.jvm.JvmStatic
        fun ofLoading(): StateData {
            return StateData(State.LOADING, null)
        }

        @kotlin.jvm.JvmStatic
        fun ofError(exception: Exception?): StateData {
            return StateData(State.ERROR, exception)
        }

        @kotlin.jvm.JvmStatic
        fun ofSuccess(): StateData {
            return StateData(State.SUCCESS, null)
        }
    }
}
