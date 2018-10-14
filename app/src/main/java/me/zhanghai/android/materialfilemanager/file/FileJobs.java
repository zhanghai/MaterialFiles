/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.filesystem.Syscall;

public interface FileJobs {

    class Copy extends FileJob {

        private List<File> mFromFiles;
        private File mToDirectory;

        public Copy(List<File> fromFiles, File toDirectory) {
            mFromFiles = fromFiles;
            mToDirectory = toDirectory;
        }

        @Override
        public void run() throws FileSystemException {
            // TODO
        }
    }

    class CreateDirectory extends FileJob {

        private File mFile;

        public CreateDirectory(File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                Syscall.createDirectory(file.getPath());
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    class CreateFile extends FileJob {

        private File mFile;

        public CreateFile(File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                Syscall.createFile(file.getPath());
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    class Delete extends FileJob {

        private List<File> mFiles;

        public Delete(List<File> files) {
            mFiles = files;
        }

        @Override
        public void run() throws FileSystemException {
            for (File file : mFiles) {
                FileSystemException.throwIfInterrupted();
                file.reloadInformation();
                deleteRecursively(file);
            }
        }

        private void deleteRecursively(File file) throws FileSystemException {
            if (file.isDirectoryDoNotFollowSymbolicLinks()) {
                List<File> children = file.getChildren();
                for (File child : children) {
                    FileSystemException.throwIfInterrupted();
                    deleteRecursively(child);
                }
            }
            delete(file);
        }

        private void delete(File file) throws FileSystemException {
            if (file instanceof LocalFile) {
                LocalFile localFile = (LocalFile) file;
                Syscall.delete(localFile.getPath());
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    class Move extends FileJob {

        private List<File> mFromFiles;
        private File mToDirectory;

        public Move(List<File> fromFiles, File toDirectory) {
            mFromFiles = fromFiles;
            mToDirectory = toDirectory;
        }

        @Override
        public void run() throws FileSystemException {
            // TODO
        }

        private void moveWithRename(File fromFile, File toDirectory) {
            // TODO: Handle into oneself or vice versa and name collision.

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
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                Syscall.rename(file.getPath(), mNewName);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }
}
