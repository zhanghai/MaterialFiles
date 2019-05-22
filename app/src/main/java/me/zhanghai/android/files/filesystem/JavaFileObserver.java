/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.util.ThrottledRunnable;

/*
 * @see com.android.internal.content.FileSystemProvider.DirectoryObserver
 */
public class JavaFileObserver extends FileObserver {

    private static final int MASK = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO | CREATE | DELETE
            | DELETE_SELF | MOVE_SELF;

    private static final long THROTTLE_INTERVAL_MILLIS = 1000;

    @NonNull
    private final ThrottledRunnable mOnChange;

    private volatile boolean mWatching;

    public JavaFileObserver(@NonNull String path, @NonNull Runnable onChange) {
        super(path, MASK);

        mOnChange = new ThrottledRunnable(() -> {
            if (mWatching) {
                onChange.run();
            }
        }, THROTTLE_INTERVAL_MILLIS, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void startWatching() {
        mWatching = true;

        super.startWatching();
    }

    @Override
    public void stopWatching() {
        mWatching = false;

        // This can call onEvent(), which should be a bug, so we set mWatching to false before
        // calling it.
        super.stopWatching();

        mOnChange.cancel();
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if (mWatching) {
            mOnChange.run();
        }
    }
}
