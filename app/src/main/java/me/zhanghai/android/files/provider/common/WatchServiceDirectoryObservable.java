/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import java8.nio.file.ClosedWatchServiceException;
import java8.nio.file.Path;
import java8.nio.file.StandardWatchEventKinds;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;

public class WatchServiceDirectoryObservable implements DirectoryObservable {

    @NonNull
    private final WatchService mWatchService;

    @NonNull
    private final Set<Runnable> mObservers = new HashSet<>();

    @NonNull
    private static final AtomicInteger sPollerId = new AtomicInteger();

    @NonNull
    private final Poller mPoller;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public WatchServiceDirectoryObservable(@NonNull Path path) throws IOException {
        mWatchService = path.getFileSystem().newWatchService();
        boolean successful = false;
        try {
            path.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            mPoller = new Poller();
            mPoller.start();
            successful = true;
        } finally {
            if (!successful) {
                mWatchService.close();
            }
        }
    }

    @Override
    public void addObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            mObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mLock) {
            ensureOpenLocked();
            mObservers.remove(observer);
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
            mPoller.interrupt();
            mObservers.clear();
            mWatchService.close();
            mClosed = true;
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
                        for (Runnable observer : mObservers) {
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
