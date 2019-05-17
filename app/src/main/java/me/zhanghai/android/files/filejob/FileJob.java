/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Random;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.ToastUtils;

public abstract class FileJob {

    private int mId = new Random().nextInt();

    private FileJobService mService;

    private Future<?> mFuture;

    public int getId() {
        return mId;
    }

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

    public void setFuture(@NonNull Future<?> future) {
        mFuture = future;
    }

    public void cancel() {
        mFuture.cancel(true);
    }
}
