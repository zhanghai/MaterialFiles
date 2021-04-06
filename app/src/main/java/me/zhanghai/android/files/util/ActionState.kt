/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

sealed class ActionState<A, R> {
    class Ready<A, R> : ActionState<A, R>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            return true
        }

        override fun hashCode(): Int = javaClass.hashCode()
    }

    data class Running<A, R>(val argument: A) : ActionState<A, R>()

    data class Success<A, R>(val argument: A, val result: R) : ActionState<A, R>()

    data class Error<A, R>(val argument: A, val throwable: Throwable) : ActionState<A, R>()
}

val ActionState<*, *>.isReady: Boolean
    get() = this is ActionState.Ready

val ActionState<*, *>.isRunning: Boolean
    get() = this is ActionState.Running

val ActionState<*, *>.isFinished: Boolean
    get() = when (this) { is ActionState.Success, is ActionState.Error -> true else -> false }
