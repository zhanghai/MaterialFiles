/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.app.Service;

import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.ToastUtils;

public abstract class FileJob {

    public void run(Service service) {
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
