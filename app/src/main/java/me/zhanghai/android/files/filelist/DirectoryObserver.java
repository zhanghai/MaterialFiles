/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Handler;
import android.os.Looper;

import java.io.Closeable;
import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.DirectoryObservable;

public class DirectoryObserver implements Closeable {

    private static final long THROTTLE_INTERVAL_MILLIS = 1000;

    @NonNull
    private final DirectoryObservable mDirectoryObservable;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public DirectoryObserver(@NonNull Path path, @MainThread @NonNull Runnable onChange)
            throws IOException {
        mDirectoryObservable = DirectoryObservable.observeDirectory(path, THROTTLE_INTERVAL_MILLIS);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mDirectoryObservable.addObserver(() -> mainHandler.post(() -> {
            synchronized (mLock) {
                if (!mClosed) {
                    onChange.run();
                }
            }
        }));
    }

    @Override
    @WorkerThread
    public void close() {
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            mClosed = true;
        }
        try {
            mDirectoryObservable.close();
        } catch (IOException e) {
            // Ignored.
            e.printStackTrace();
        }
    }
}
