/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.settings.Settings;

public interface RootablePath {

    boolean shouldPreferRoot();

    void setPreferRoot();

    @NonNull
    default RootStrategy getRootStrategy() {
        if (RootUtils.isRunningAsRoot()) {
            return RootStrategy.NEVER;
        }
        RootStrategy strategy = Settings.ROOT_STRATEGY.getValue();
        if (strategy == RootStrategy.PREFER_NO && shouldPreferRoot()) {
            return RootStrategy.PREFER_YES;
        }
        return strategy;
    }
}
