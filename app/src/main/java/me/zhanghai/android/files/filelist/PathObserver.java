/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.Closeable;
import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class PathObserver implements Closeable {

    private static final long THROTTLE_INTERVAL_MILLIS = 1000;

    @Nullable
    private PathObservable mPathObservable;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public PathObserver(@NonNull Path path, @MainThread @NonNull Runnable onChange) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            synchronized (mLock) {
                if (mClosed) {
                    return;
                }
                try {
                    mPathObservable = MoreFiles.observePath(path,
                            THROTTLE_INTERVAL_MILLIS);
                } catch (UnsupportedOperationException e) {
                    // Ignored.
                    return;
                } catch (IOException e) {
                    // Ignored.
                    e.printStackTrace();
                    return;
                }
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mPathObservable.addObserver(() -> mainHandler.post(onChange));
            }
        });
    }

    @Override
    @WorkerThread
    public void close() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            synchronized (mLock) {
                if (mClosed) {
                    return;
                }
                mClosed = true;
                if (mPathObservable != null) {
                    try {
                        mPathObservable.close();
                    } catch (IOException e) {
                        // Ignored.
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
