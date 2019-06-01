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
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.ClosedWatchServiceException;
import java8.nio.file.Path;
import java8.nio.file.StandardWatchEventKinds;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;
import me.zhanghai.android.files.util.ThrottledRunnable;

public class WatchServiceDirectoryObservable implements DirectoryObservable {

    private final long mIntervalMillis;

    private final WatchService mWatchService;

    @NonNull
    private final Map<Object, ThrottledRunnable> mObservers = new HashMap<>();

    @NonNull
    private static final AtomicInteger sNotifierId = new AtomicInteger();
    private final Notifier mNotifier;

    @NonNull
    private static final AtomicInteger sPollerId = new AtomicInteger();
    private final Poller mPoller;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public WatchServiceDirectoryObservable(@NonNull Path path, long intervalMillis)
            throws IOException {
        mIntervalMillis = intervalMillis;
        boolean successful = false;
        try {
            mWatchService = path.getFileSystem().newWatchService();
            path.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            mNotifier = new Notifier();
            mNotifier.start();
            mPoller = new Poller();
            mPoller.start();
            successful = true;
        } finally {
            if (!successful) {
                close();
            }
        }
    }

    @Override
    public void addObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            mObservers.put(observer, new ThrottledRunnable(observer, mIntervalMillis,
                    mNotifier.getHandler()));
        }
    }

    @Override
    public void removeObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            ThrottledRunnable throttledObserver = mObservers.remove(observer);
            if (throttledObserver != null) {
                throttledObserver.cancel();
            }
        }
    }

    private void ensureOpenLocked() {
        if (mClosed) {
            throw new ClosedDirectoryObserverException();
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            if (mPoller != null) {
                mPoller.interrupt();
            }
            if (mNotifier != null) {
                mNotifier.quit();
            }
            for (ThrottledRunnable observer : mObservers.values()) {
                observer.cancel();
            }
            mObservers.clear();
            if (mWatchService != null) {
                mWatchService.close();
            }
            mClosed = true;
        }
    }

    private class Notifier extends HandlerThread {

        @Nullable
        private Handler mHandler;

        @NonNull
        private final Object mLock = new Object();

        Notifier() {
            super("WatchServiceDirectoryObservable.Notifier-" + sNotifierId.getAndIncrement());

            setDaemon(true);
        }

        @NonNull
        Handler getHandler() {
            synchronized (mLock) {
                if (mHandler == null) {
                    mHandler = new Handler(getLooper());
                }
                return mHandler;
            }
        }
    }

    private class Poller extends Thread {

        Poller() {
            setName("WatchServiceDirectoryObservable.Poller-" + sPollerId.getAndIncrement());
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                WatchKey key;
                try {
                    key = mWatchService.take();
                } catch (ClosedWatchServiceException | InterruptedException e) {
                    break;
                }
                boolean changed = !key.pollEvents().isEmpty();
                if (changed) {
                    synchronized (mLock) {
                        for (ThrottledRunnable observer : mObservers.values()) {
                            observer.run();
                        }
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
}
