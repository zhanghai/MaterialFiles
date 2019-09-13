/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.util.ThrottledRunnable;

public abstract class AbstractPathObservable implements PathObservable {

    private static final Notifier sNotifier;
    static {
        sNotifier = new Notifier();
        sNotifier.start();
    }

    private final long mIntervalMillis;

    @NonNull
    private final Map<Object, ThrottledRunnable> mObservers = new HashMap<>();

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public AbstractPathObservable(long intervalMillis) {
        mIntervalMillis = intervalMillis;
    }

    @NonNull
    protected static Handler getHandler() {
        return sNotifier.getHandler();
    }

    @Override
    public final void addObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            mObservers.put(observer, new ThrottledRunnable(observer, mIntervalMillis,
                    getHandler()));
        }
    }

    @Override
    public final void removeObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            ThrottledRunnable throttledObserver = mObservers.remove(observer);
            if (throttledObserver != null) {
                throttledObserver.cancel();
            }
        }
    }

    protected void notifyObservers() {
        synchronized (mLock) {
            for (ThrottledRunnable observer : mObservers.values()) {
                observer.run();
            }
        }
    }

    private void ensureOpenLocked() {
        if (mClosed) {
            throw new ClosedDirectoryObserverException();
        }
    }

    @Override
    public final void close() throws IOException {
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            for (ThrottledRunnable observer : mObservers.values()) {
                observer.cancel();
            }
            mObservers.clear();
            onCloseLocked();
            mClosed = true;
        }
    }

    protected abstract void onCloseLocked() throws IOException;

    private static class Notifier extends HandlerThread {

        @Nullable
        private Handler mHandler;
        @NonNull
        private final Object mHandlerLock = new Object();

        Notifier() {
            super("AbstractPathObservable.Notifier");

            setDaemon(true);
        }

        @NonNull
        Handler getHandler() {
            synchronized (mHandlerLock) {
                if (mHandler == null) {
                    mHandler = new Handler(getLooper());
                }
                return mHandler;
            }
        }
    }
}
