/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.filesystem.Syscall;
import me.zhanghai.android.materialfilemanager.util.ExceptionUtils;

public interface FileJobs {

    class Copy extends FileJob {

        @NonNull
        private final List<File> mFromFiles;
        @NonNull
        private final File mToDirectory;

        public Copy(@NonNull List<File> fromFiles, @NonNull File toDirectory) {
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

        private static void copy(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
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

        @NonNull
        private final File mFile;

        public CreateDirectory(@NonNull File file) {
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

        @NonNull
        private final File mFile;

        public CreateFile(@NonNull File file) {
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

        @NonNull
        private final List<File> mFiles;

        public Delete(@NonNull List<File> files) {
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

        private static void deleteRecursively(@NonNull File file) throws FileSystemException,
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

        private static void delete(@NonNull File file) throws FileSystemException {
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

        @NonNull
        private final List<File> mFromFiles;
        @NonNull
        private final File mToDirectory;

        public Move(@NonNull List<File> fromFiles, @NonNull File toDirectory) {
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

        private static void rename(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException {
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

        private static void move(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
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

        @NonNull
        private final File mFile;
        @NonNull
        private final String mNewName;

        public Rename(@NonNull File file, @NonNull String newName) {
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
