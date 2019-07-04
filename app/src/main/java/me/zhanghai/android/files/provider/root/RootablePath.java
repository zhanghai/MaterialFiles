/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import androidx.annotation.NonNull;

public interface RootablePath {

    boolean shouldPreferRoot();

    void setPreferRoot();

    @NonNull
    default RootStrategy getRootStrategy() {
        if (RootUtils.isRunningAsRoot()) {
            return RootStrategy.NEVER;
        }
        // TODO: Get global strategy.
        RootStrategy strategy = RootStrategy.PREFER_NO;
        if (strategy == RootStrategy.PREFER_NO && shouldPreferRoot()) {
            return RootStrategy.PREFER_YES;
        }
        return strategy;
    }
}
