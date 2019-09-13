/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import java8.nio.file.ClosedWatchServiceException;
import java8.nio.file.Path;
import java8.nio.file.StandardWatchEventKinds;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;

public class WatchServicePathObservable extends AbstractPathObservable {

    private final WatchService mWatchService;

    @NonNull
    private static final AtomicInteger sPollerId = new AtomicInteger();
    private final Poller mPoller;

    public WatchServicePathObservable(@NonNull Path path, long intervalMillis)
            throws IOException {
        super(intervalMillis);

        boolean successful = false;
        try {
            mWatchService = path.getFileSystem().newWatchService();
            path.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

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
    protected void onCloseLocked() throws IOException {
        if (mPoller != null) {
            mPoller.interrupt();
        }
        if (mWatchService != null) {
            mWatchService.close();
        }
    }

    private class Poller extends Thread {

        Poller() {
            super("WatchServicePathObservable.Poller-" + sPollerId.getAndIncrement());

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
                    notifyObservers();
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
}
