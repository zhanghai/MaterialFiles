/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.os.FileObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.util.AppUtils;

/*
 * @see com.android.internal.content.FileSystemProvider.DirectoryObserver
 */
public class JavaFileObserver extends FileObserver {

    private static final int MASK = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO | CREATE | DELETE
            | DELETE_SELF | MOVE_SELF;

    @NonNull
    private final Runnable mOnChange;

    private boolean mWatching;

    public JavaFileObserver(@NonNull String path, @NonNull Runnable onChange) {
        super(path, MASK);

        mOnChange = onChange;
    }

    @Override
    public void startWatching() {
        super.startWatching();

        mWatching = true;
    }

    @Override
    public void stopWatching() {
        mWatching = false;

        // This can call onEvent(), which should be a bug, so we work around it with mWatching.
        super.stopWatching();
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if (mWatching) {
            AppUtils.runOnUiThread(mOnChange);
        }
    }
}
