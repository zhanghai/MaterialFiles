/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.ClosedWatchServiceException;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;
import java8.nio.file.Watchable;

public abstract class AbstractWatchService implements WatchService {

    private static final WatchKey KEY_CLOSED = new DummyKey();

    @NonNull
    private final LinkedBlockingQueue<WatchKey> mQueue = new LinkedBlockingQueue<>();

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    void enqueue(@NonNull AbstractWatchKey key) {
        mQueue.offer(key);
    }

    @Nullable
    @Override
    public WatchKey poll() {
        checkClosed();
        return checkClosedKey(mQueue.poll());
    }

    @Nullable
    @Override
    public WatchKey poll(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(unit);
        checkClosed();
        return checkClosedKey(mQueue.poll(timeout, unit));
    }

    @NonNull
    @Override
    public WatchKey take() throws InterruptedException {
        checkClosed();
        return checkClosedKey(mQueue.take());
    }

    private WatchKey checkClosedKey(@Nullable WatchKey key) {
        if (key == KEY_CLOSED) {
            // There may be other threads still waiting for a key.
            mQueue.offer(key);
        }
        checkClosed();
        return key;
    }

    private void checkClosed() {
        if (mClosed) {
            throw new ClosedWatchServiceException();
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            onClose();
            mQueue.clear();
            mQueue.offer(KEY_CLOSED);
            mClosed = true;
        }
    }

    protected abstract void onClose() throws IOException;

    private static class DummyKey implements WatchKey {

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public List<WatchEvent<?>> pollEvents() {
            return null;
        }

        @Override
        public boolean reset() {
            return false;
        }

        @Override
        public void cancel() {}

        @Override
        public Watchable watchable() {
            return null;
        }
    }
}
