/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.app.Service;

import java.util.List;
import java.util.concurrent.CancellationException;

import me.zhanghai.android.materialfilemanager.filesystem.FileOperation;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.ToastUtils;

public abstract class FileJob {

    public void run(Service service) {
        try {
            List<FileOperation> operations = prepareOperations();
            for (FileOperation operation : operations) {
                operation.run();
            }
            // TODO: Toast
        } catch (CancellationException e) {
            // TODO
            e.printStackTrace();
        } catch (FileSystemException e) {
            e.printStackTrace();
            AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(service), service));
        }
    }

    public abstract List<FileOperation> prepareOperations() throws FileSystemException;
}
