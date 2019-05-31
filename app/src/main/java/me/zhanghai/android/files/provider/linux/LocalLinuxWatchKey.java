/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.AbstractWatchKey;

class LocalLinuxWatchKey extends AbstractWatchKey {

    private final int mWd;

    private volatile boolean mValid = true;

    LocalLinuxWatchKey(@NonNull LocalLinuxWatchService watchService, @NonNull LinuxPath path,
                       int wd) {
        super(watchService, path);

        mWd = wd;
    }

    int getWatchDescriptor() {
        return mWd;
    }

    @Override
    public boolean isValid() {
        return mValid;
    }

    void setInvalid() {
        mValid = false;
    }

    @Override
    public void cancel() {
        if (mValid) {
            getWatcherService().cancel(this);
        }
    }

    @NonNull
    @Override
    public LinuxPath watchable() {
        return (LinuxPath) super.watchable();
    }

    @NonNull
    @Override
    protected LocalLinuxWatchService getWatcherService() {
        return (LocalLinuxWatchService) super.getWatcherService();
    }
}
