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

    class CreateDirectory extends FileJob {

        private File mFile;

        public CreateDirectory(File file) {
            mFile = file;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.createDirectory(mFile);
        }
    }

    class CreateFile extends FileJob {

        private File mFile;

        public CreateFile(File file) {
            mFile = file;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.createFile(mFile);
        }
    }

    class Delete extends FileJob {

        private List<File> mFiles;

        public Delete(List<File> files) {
            mFiles = files;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.delete(mFiles);
        }
    }

    class Rename extends FileJob {

        private File mFile;
        private String mNewName;

        public Rename(File file, String newName) {
            mFile = file;
            mNewName = newName;
        }

        @Override
        public List<FileOperation> prepareOperations() throws FileSystemException {
            return FileOperations.rename(mFile, mNewName);
        }
    }
}
