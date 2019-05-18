/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import androidx.annotation.NonNull;
import java8.nio.file.CopyOption;
import java9.util.function.LongConsumer;

public class ProgressCopyOption implements CopyOption {

    public static final int DEFAULT_INTERVAL_MILLIS = 200;

    @NonNull
    private final LongConsumer mListener;

    private final int mIntervalMillis;

    public ProgressCopyOption(@NonNull LongConsumer listener, int intervalMillis) {
        mListener = listener;
        mIntervalMillis = intervalMillis;
    }

    public ProgressCopyOption(@NonNull LongConsumer listener) {
        this(listener, DEFAULT_INTERVAL_MILLIS);
    }

    @NonNull
    public LongConsumer getListener() {
        return mListener;
    }

    public int getIntervalMillis() {
        return mIntervalMillis;
    }
}
