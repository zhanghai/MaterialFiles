/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import java.io.IOException;
import java.io.InterruptedIOException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.ToastUtils;

public abstract class FileJob {

    private FileJobService mService;

    public void run(@NonNull FileJobService service) {
        mService = service;
        try {
            run();
            // TODO: Toast
        } catch (InterruptedIOException e) {
            // TODO
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(), service));
        } finally {
            mService = null;
        }
    }

    protected abstract void run() throws IOException;

    protected FileJobService getService() {
        return mService;
    }
}
