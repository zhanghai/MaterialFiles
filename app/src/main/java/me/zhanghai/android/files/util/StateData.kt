/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

@Deprecated("", ReplaceWith("ActionState"))
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

        @JvmStatic
        fun ofLoading(): StateData {
            return StateData(State.LOADING, null)
        }

        @JvmStatic
        fun ofError(exception: Exception?): StateData {
            return StateData(State.ERROR, exception)
        }

        @JvmStatic
        fun ofSuccess(): StateData {
            return StateData(State.SUCCESS, null)
        }
    }
}
