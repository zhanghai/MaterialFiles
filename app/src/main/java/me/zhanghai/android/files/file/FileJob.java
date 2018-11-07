/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.app.Service;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.filesystem.FileSystemException;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.ToastUtils;

public abstract class FileJob {

    public void run(@NonNull Service service) {
        try {
            run();
            // TODO: Toast
        } catch (InterruptedException e) {
            // TODO
            e.printStackTrace();
        } catch (FileSystemException e) {
            e.printStackTrace();
            AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(service), service));
        }
    }

    protected abstract void run() throws FileSystemException, InterruptedException;
}
