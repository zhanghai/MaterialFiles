/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.Closeable;

import androidx.annotation.NonNull;

public interface PathObservable extends Closeable {

    void addObserver(@NonNull Runnable observer);

    void removeObserver(@NonNull Runnable observer);
}
