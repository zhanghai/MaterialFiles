/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;

public class ThrottledRunnable implements Runnable {

    @NonNull
    private final Runnable mRunnable;
    private final long mIntervalMillis;
    @NonNull
    private final Handler mHandler;

    @NonNull
    private final Object mLock = new Object();

    private long mScheduledUptimeMillis;

    public ThrottledRunnable(@NonNull Runnable runnable, long intervalMillis,
                             @NonNull Handler handler) {
        mRunnable = runnable;
        mIntervalMillis = intervalMillis;
        mHandler = handler;
    }

    @Override
    public void run() {
        synchronized (mLock) {
            long currentUptimeMillis = SystemClock.uptimeMillis();
            if (mScheduledUptimeMillis + mIntervalMillis < currentUptimeMillis) {
                mScheduledUptimeMillis = 0;
            }
            if (mScheduledUptimeMillis == 0) {
                mScheduledUptimeMillis = currentUptimeMillis;
                mHandler.post(mRunnable);
            } else if (mScheduledUptimeMillis <= currentUptimeMillis) {
                mScheduledUptimeMillis = mScheduledUptimeMillis + mIntervalMillis;
                mHandler.postAtTime(mRunnable, mScheduledUptimeMillis);
            } else {
                // We've been scheduled, nothing to do now.
            }
        }
    }

    public void cancel() {
        synchronized (mLock) {
            mScheduledUptimeMillis = 0;
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
