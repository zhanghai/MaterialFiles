/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.os.Handler
import android.os.SystemClock

class ThrottledRunnable(
    private val handler: Handler,
    private val intervalMillis: Long,
    block: () -> Unit
) : () -> Unit {
    private val lock = Any()

    private val runnable = Runnable(block)

    private var scheduledUptimeMillis = 0L

    override operator fun invoke() {
        synchronized(lock) {
            val currentUptimeMillis = SystemClock.uptimeMillis()
            if (scheduledUptimeMillis + intervalMillis < currentUptimeMillis) {
                scheduledUptimeMillis = 0
            }
            when {
                scheduledUptimeMillis == 0L -> {
                    scheduledUptimeMillis = currentUptimeMillis
                    handler.post(runnable)
                }
                scheduledUptimeMillis <= currentUptimeMillis -> {
                    scheduledUptimeMillis += intervalMillis
                    handler.postAtTime(runnable, scheduledUptimeMillis)
                }
                // We've been scheduled, nothing to do now.
                else -> {}
            }
        }
    }

    fun cancel() {
        synchronized(lock) {
            scheduledUptimeMillis = 0
            handler.removeCallbacks(runnable)
        }
    }
}
