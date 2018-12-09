/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.filesystem.FileSystemException;
import me.zhanghai.android.files.filesystem.LocalFile;
import me.zhanghai.android.files.filesystem.Syscall;
import me.zhanghai.android.files.util.ExceptionUtils;

public class FileJobs {

    private FileJobs() {}

    private abstract static class Base extends FileJob {

        protected static void copy(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
            copyOrMove(fromFile, toDirectory, true);
        }

        // @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c
        //      copy_move_file
        private static void copyOrMove(@NonNull File fromFile, @NonNull File toDirectory,
                                       boolean copy) throws FileSystemException,
                InterruptedException {
            if (fromFile.isAncestorOfOrEqualTo(toDirectory)) {
                // Don't allow copy/move into the source itself.
                // TODO: Prompt skip, skip-all or abort.
                throw new FileSystemException(new IllegalArgumentException(
                        "Cannot copy/move a folder into itself"));
            }
            File toFile = toDirectory.getChild(fromFile.getName());
            if (toFile.isAncestorOfOrEqualTo(fromFile)) {
                // Don't allow copy/move over the source itself or its ancestors.
                // TODO: Prompt skip, skip-all or abort.
                throw new FileSystemException(new IllegalArgumentException(
                        "Cannot copy/move over a file over itself"));
            }
            boolean overwrite = false;
            if (fromFile instanceof LocalFile && toFile instanceof LocalFile) {
                LocalFile fromLocalFile = (LocalFile) fromFile;
                LocalFile toLocalFile = (LocalFile) toFile;
                String fromPath = fromLocalFile.getPath();
                String toPath = toLocalFile.getPath();
                boolean retry;
                do {
                    retry = false;
                    try {
                        if (copy) {
                            Syscall.copy(fromPath, toPath, overwrite, 1024 * 1024, null);
                        } else {
                            Syscall.move(fromPath, toPath, overwrite, 1024 * 1024, null);
                        }
                    } catch (FileSystemException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof ErrnoException) {
                            ErrnoException errnoException = (ErrnoException) cause;
                            if (!overwrite && errnoException.errno == OsConstants.EEXIST) {
                                // TODO: Prompt overwrite, skip, skip-all or abort, or merge.
                                if (false) {
                                    overwrite = true;
                                    retry = true;
                                    continue;
                                }
                            }
                            if (errnoException.errno == OsConstants.EINVAL) {
                                // TODO: Prompt invalid name.
                                if (false) {
                                    retry = true;
                                    continue;
                                }
                            }
                        }
                        throw e;
                    }
                } while (retry);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }

        protected void createDirectory(@NonNull File file) throws FileSystemException {
            if (file instanceof LocalFile) {
                LocalFile localFile = (LocalFile) file;
                String path = localFile.getPath();
                Syscall.createDirectory(path);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }

        protected void createFile(@NonNull File file) throws FileSystemException {
            if (file instanceof LocalFile) {
                LocalFile localFile = (LocalFile) file;
                String path = localFile.getPath();
                Syscall.createFile(path);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }

        protected static void delete(@NonNull File file) throws FileSystemException {
            if (file instanceof LocalFile) {
                LocalFile localFile = (LocalFile) file;
                Syscall.delete(localFile.getPath());
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }

        protected static void move(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
            copyOrMove(fromFile, toDirectory, false);
        }

        protected static void moveByRename(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException {
            File toFile = toDirectory.getChild(fromFile.getName());
            rename(fromFile, toFile);
        }

        protected static void rename(@NonNull File fromFile, @NonNull File toFile)
                throws FileSystemException {
            if (fromFile instanceof LocalFile && toFile instanceof LocalFile) {
                LocalFile fromLocalFile = (LocalFile) fromFile;
                LocalFile toLocalFile = (LocalFile) toFile;
                String fromPath = fromLocalFile.getPath();
                String toPath = toLocalFile.getPath();
                Syscall.rename(fromPath, toPath);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class Copy extends Base {

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
                fromFile.reloadInformation();
                copyRecursively(fromFile, mToDirectory);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void copyRecursively(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
            copy(fromFile, toDirectory);
            ExceptionUtils.throwIfInterrupted();
            if (fromFile.isDirectoryDoNotFollowSymbolicLinks()) {
                List<File> children = fromFile.getChildren();
                ExceptionUtils.throwIfInterrupted();
                for (File child : children) {
                    copyRecursively(child, toDirectory);
                    ExceptionUtils.throwIfInterrupted();
                }
            }
        }
    }

    public static class CreateDirectory extends Base {

        @NonNull
        private final File mFile;

        public CreateDirectory(@NonNull File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            createDirectory(mFile);
        }
    }

    public static class CreateFile extends Base {

        @NonNull
        private final File mFile;

        public CreateFile(@NonNull File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            createFile(mFile);
        }
    }

    public static class Delete extends Base {

        @NonNull
        private final List<File> mFiles;

        public Delete(@NonNull List<File> files) {
            mFiles = files;
        }

        @Override
        public void run() throws FileSystemException, InterruptedException {
            for (File file : mFiles) {
                file.reloadInformation();
                ExceptionUtils.throwIfInterrupted();
                deleteRecursively(file);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void deleteRecursively(@NonNull File file) throws FileSystemException,
                InterruptedException {
            if (file.isDirectoryDoNotFollowSymbolicLinks()) {
                List<File> children = file.getChildren();
                ExceptionUtils.throwIfInterrupted();
                for (File child : children) {
                    deleteRecursively(child);
                    ExceptionUtils.throwIfInterrupted();
                }
            }
            delete(file);
        }
    }

    public static class Move extends Base {

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
                    moveByRename(fromFile, mToDirectory);
                } catch (FileSystemException e) {
                    fromFilesToMove.add(fromFile);
                }
                ExceptionUtils.throwIfInterrupted();
            }
            for (File fromFile : fromFilesToMove) {
                fromFile.reloadInformation();
                moveRecursively(fromFile, mToDirectory);
                ExceptionUtils.throwIfInterrupted();
            }
        }

        private static void moveRecursively(@NonNull File fromFile, @NonNull File toDirectory)
                throws FileSystemException, InterruptedException {
            if (fromFile.isDirectoryDoNotFollowSymbolicLinks()) {
                try {
                    moveByRename(fromFile, toDirectory);
                    return;
                } catch (FileSystemException e) {
                    // Ignored
                }
                ExceptionUtils.throwIfInterrupted();
                copy(fromFile, toDirectory);
                ExceptionUtils.throwIfInterrupted();
                List<File> children = fromFile.getChildren();
                ExceptionUtils.throwIfInterrupted();
                for (File child : children) {
                    moveRecursively(child, toDirectory);
                    ExceptionUtils.throwIfInterrupted();
                }
                delete(fromFile);
            } else {
                move(fromFile, toDirectory);
            }
        }
    }

    public static class Rename extends Base {

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
            File newFile = mFile.getSibling(mNewName);
            rename(mFile, newFile);
        }
    }
}
