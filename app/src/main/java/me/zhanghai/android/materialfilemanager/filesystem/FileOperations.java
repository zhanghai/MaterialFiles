/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileOperations {

    public static List<FileOperation> createDirectory(File file) throws FileSystemException {
        return Collections.singletonList(new CreateDirectory(file));
    }

    public static List<FileOperation> createFile(File file) throws FileSystemException {
        return Collections.singletonList(new CreateFile(file));
    }

    public static List<FileOperation> delete(List<File> files) throws FileSystemException {
        List<FileOperation> operations = new ArrayList<>();
        for (File file : files) {
            file.reloadInformation();
            delete(file, operations);
        }
        return operations;
    }

    private static void delete(File file, List<FileOperation> operations)
            throws FileSystemException {
        if (file.isDirectoryDoNotFollowSymbolicLinks()) {
            List<File> children = file.getChildren();
            for (File child : children) {
                delete(child, operations);
            }
        }
        operations.add(new Delete(file));
    }

    public static List<FileOperation> rename(File file, String newName) throws FileSystemException {
        return Collections.singletonList(new Rename(file, newName));
    }

    private static class CreateDirectory implements FileOperation {

        private File mFile;

        public CreateDirectory(File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                LocalFileOperationStrategies.SYSCALL_STRATEGY.createDirectory(file);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class CreateFile implements FileOperation {

        private File mFile;

        public CreateFile(File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                LocalFileOperationStrategies.SYSCALL_STRATEGY.createFile(file);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class Delete implements FileOperation {

        private File mFile;

        public Delete(File file) {
            mFile = file;
        }

        @Override
        public void run() throws FileSystemException {
            if (mFile instanceof LocalFile) {
                LocalFile file = (LocalFile) mFile;
                LocalFileOperationStrategies.SYSCALL_STRATEGY.delete(file);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class Rename implements FileOperation {

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
                LocalFileOperationStrategies.SYSCALL_STRATEGY.rename(file, mNewName);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }
}
