/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.ViewPropertyAnimator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun Animator.awaitEnd(): Unit =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        addListener(object : AnimatorListenerAdapter() {
            private var canceled = false

            override fun onAnimationCancel(animation: Animator) {
                canceled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                removeListener(this)
                if (continuation.isActive) {
                    if (canceled) {
                        continuation.cancel()
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }
        })
    }

suspend fun ViewPropertyAnimator.awaitEnd(): Unit =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        setListener(object : AnimatorListenerAdapter() {
            private var canceled = false

            override fun onAnimationCancel(animation: Animator) {
                canceled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                setListener(null)
                if (continuation.isActive) {
                    if (canceled) {
                        continuation.cancel()
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }
        })
    }
