/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.filesystem.Syscall;
import me.zhanghai.android.materialfilemanager.util.ExceptionUtils;

public interface FileJobs {

    class Copy extends FileJob {

        private List<File> mFromFiles;
        private File mToDirectory;

        public Copy(List<File> fromFiles, File toDirectory) {
            mFromFiles = fromFiles;
            mToDirectory = toDirectory;
        }

        @Override
        public void run() throws FileSystemException, InterruptedException {
            for (File fromFile : mFromFiles) {
                copy(fromFile, mToDirectory);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void copy(File fromFile, File toDirectory) throws FileSystemException,
                InterruptedException {
            // TODO: Handle into oneself or vice versa and name collision.
            if (fromFile instanceof LocalFile && toDirectory instanceof LocalFile) {
                LocalFile fromLocalFile = (LocalFile) fromFile;
                LocalFile toLocalDirectory = (LocalFile) toDirectory;
                String fromPath = fromLocalFile.getPath();
                // TODO: Handle target FS name restriction.
                String toPath = toLocalDirectory.getChild(fromLocalFile.getName()).getPath();
                // TODO
                Syscall.copy(fromPath, toPath, 1024 * 1024, null);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
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
        public void run() throws FileSystemException, InterruptedException {
            for (File file : mFiles) {
                file.reloadInformation();
                deleteRecursively(file);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void deleteRecursively(File file) throws FileSystemException,
                InterruptedException {
            if (file.isDirectoryDoNotFollowSymbolicLinks()) {
                List<File> children = file.getChildren();
                for (File child : children) {
                    deleteRecursively(child);
                    ExceptionUtils.throwIfInterrupted();
                }
            }
            delete(file);
        }

        private static void delete(File file) throws FileSystemException {
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
        public void run() throws FileSystemException, InterruptedException {
            List<File> fromFilesToMove = new ArrayList<>();
            for (File fromFile : mFromFiles) {
                try {
                    rename(fromFile, mToDirectory);
                } catch (FileSystemException e) {
                    fromFilesToMove.add(fromFile);
                }
                ExceptionUtils.throwIfInterrupted();
            }
            for (File fromFile : fromFilesToMove) {
                move(fromFile, mToDirectory);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void rename(File fromFile, File toDirectory) throws FileSystemException {
            // TODO: Handle into oneself or vice versa and name collision.
            if (fromFile instanceof LocalFile && toDirectory instanceof LocalFile) {
                LocalFile fromLocalFile = (LocalFile) fromFile;
                LocalFile toLocalDirectory = (LocalFile) toDirectory;
                String fromPath = fromLocalFile.getPath();
                // TODO: Handle target FS name restriction.
                String toPath = toLocalDirectory.getChild(fromLocalFile.getName()).getPath();
                Syscall.rename(fromPath, toPath);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }

        private static void move(File fromFile, File toDirectory) throws FileSystemException,
                InterruptedException {
            // TODO: Handle into oneself or vice versa and name collision.
            if (fromFile instanceof LocalFile && toDirectory instanceof LocalFile) {
                LocalFile fromLocalFile = (LocalFile) fromFile;
                LocalFile toLocalDirectory = (LocalFile) toDirectory;
                String fromPath = fromLocalFile.getPath();
                // TODO: Handle target FS name restriction.
                String toPath = toLocalDirectory.getChild(fromLocalFile.getName()).getPath();
                // TODO
                Syscall.move(fromPath, toPath, 1024 * 1024, null);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
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
                String newPath = file.getParent().getChild(mNewName).getPath();
                Syscall.rename(file.getPath(), newPath);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }
}
