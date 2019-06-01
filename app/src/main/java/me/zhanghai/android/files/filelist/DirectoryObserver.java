/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Handler;
import android.os.Looper;

import java.io.Closeable;
import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.DirectoryObservable;
import me.zhanghai.android.files.util.ThrottledRunnable;

public class DirectoryObserver implements Closeable {

    private static final long THROTTLE_INTERVAL_MILLIS = 1000;

    @NonNull
    private final DirectoryObservable mDirectoryObservable;

    private boolean mClosed;

    @NonNull
    private final Object mLock = new Object();

    public DirectoryObserver(@NonNull Path path, @NonNull Runnable onChange) throws IOException {
        mDirectoryObservable = DirectoryObservable.observeDirectory(path);
        mDirectoryObservable.addObserver(new ThrottledRunnable(() -> {
            synchronized (mLock) {
                if (!mClosed) {
                    onChange.run();
                }
            }
        }, THROTTLE_INTERVAL_MILLIS, new Handler(Looper.getMainLooper())));
    }

    @Override
    public void close() {
        synchronized (mLock) {
            if (mClosed) {
                return;
            }
            try {
                mDirectoryObservable.close();
            } catch (IOException e) {
                // Ignored.
                e.printStackTrace();
            }
            mClosed = true;
        }
    }
}
