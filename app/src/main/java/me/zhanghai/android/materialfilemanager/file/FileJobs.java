/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileOperation;
import me.zhanghai.android.materialfilemanager.filesystem.FileOperations;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;

public interface FileJobs {

    class CreateDirectoryJob extends FileJob {

        private File mFile;

        public CreateDirectoryJob(File file) {
            mFile = file;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.createDirectory(mFile);
        }
    }

    class CreateFileJob extends FileJob {

        private File mFile;

        public CreateFileJob(File file) {
            mFile = file;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.createFile(mFile);
        }
    }

    class DeleteFileJob extends FileJob {

        private File mFile;

        public DeleteFileJob(File file) {
            mFile = file;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.delete(mFile);
        }
    }

    class RenameJob extends FileJob {

        private File mFile;
        private String mNewName;

        public RenameJob(File file, String newName) {
            mFile = file;
            mNewName = newName;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.rename(mFile, mNewName);
        }
    }
}
