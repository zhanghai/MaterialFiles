/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.app.Service;

import java.util.concurrent.CancellationException;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileOperations;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.ToastUtils;

public interface FileJobs {

    interface Job {
        void run(Service service);
    }

    class RenameJob implements Job {

        private File mFile;
        private String mName;

        public RenameJob(File file, String name) {
            mFile = file;
            mName = name;
        }

        @Override
        public void run(Service service) {
            try {
                FileOperations.rename(mFile, mName);
                // TODO: Toast
            } catch (CancellationException e) {
                // TODO
                e.printStackTrace();
            } catch (FileSystemException e) {
                e.printStackTrace();
                AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(service), service));
            }
        }
    }

    class CreateFileJob implements Job {

        private File mFile;

        public CreateFileJob(File file) {
            mFile = file;
        }

        @Override
        public void run(Service service) {
            try {
                FileOperations.createFile(mFile);
                // TODO: Toast
            } catch (CancellationException e) {
                // TODO
                e.printStackTrace();
            } catch (FileSystemException e) {
                e.printStackTrace();
                AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(service), service));
            }
        }
    }

    class CreateDirectoryJob implements Job {

        private File mFile;

        public CreateDirectoryJob(File file) {
            mFile = file;
        }

        @Override
        public void run(Service service) {
            try {
                FileOperations.createDirectory(mFile);
                // TODO: Toast
            } catch (CancellationException e) {
                // TODO
                e.printStackTrace();
            } catch (FileSystemException e) {
                e.printStackTrace();
                AppUtils.runOnUiThread(() -> ToastUtils.show(e.getMessage(service), service));
            }
        }
    }
}
