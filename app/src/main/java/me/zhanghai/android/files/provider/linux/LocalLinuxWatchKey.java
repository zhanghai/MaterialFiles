/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.AbstractWatchKey;

class LocalLinuxWatchKey extends AbstractWatchKey {

    private final int mWd;

    private boolean mValid = true;

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
        synchronized (mLock) {
            return mValid;
        }
    }

    void setInvalid() {
        synchronized (mLock) {
            mValid = false;
        }
    }

    @Override
    public void cancel() {
        synchronized (mLock) {
            if (mValid) {
                getWatchService().cancel(this);
            }
        }
    }

    @NonNull
    @Override
    public LinuxPath watchable() {
        return (LinuxPath) super.watchable();
    }

    @NonNull
    @Override
    protected LocalLinuxWatchService getWatchService() {
        return (LocalLinuxWatchService) super.getWatchService();
    }
}
