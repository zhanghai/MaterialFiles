/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Handler;

import androidx.annotation.NonNull;

public class DebouncedRunnable implements Runnable {

    @NonNull
    private final Runnable mRunnable;
    private final long mIntervalMillis;
    @NonNull
    private final Handler mHandler;

    @NonNull
    private final Object mLock = new Object();

    public DebouncedRunnable(@NonNull Runnable runnable, long intervalMillis,
                             @NonNull Handler handler) {
        mRunnable = runnable;
        mIntervalMillis = intervalMillis;
        mHandler = handler;
    }

    @Override
    public void run() {
        synchronized (mLock) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, mIntervalMillis);
        }
    }

    public void cancel() {
        synchronized (mLock) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
