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
            delete(file, operations);
        }
        return operations;
    }

    private static void delete(File file, List<FileOperation> operations)
            throws FileSystemException {
        file.loadInformation();
        // FIXME: Symbolic links
        if (file.isDirectory()) {
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
            if (mFile instanceof JavaFileLocalFile) {
                JavaFileLocalFile file = (JavaFileLocalFile) mFile;
                JavaFile.createDirectory(file.makeJavaFile());
            } else if (mFile instanceof AndroidOsLocalFile) {
                AndroidOsLocalFile file = (AndroidOsLocalFile) mFile;
                AndroidOs.createDirectory(file.getPath());
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
            if (mFile instanceof JavaFileLocalFile) {
                JavaFileLocalFile file = (JavaFileLocalFile) mFile;
                JavaFile.createFile(file.makeJavaFile());
            } else if (mFile instanceof AndroidOsLocalFile) {
                AndroidOsLocalFile file = (AndroidOsLocalFile) mFile;
                AndroidOs.createFile(file.getPath());
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
            if (mFile instanceof JavaFileLocalFile) {
                JavaFileLocalFile file = (JavaFileLocalFile) mFile;
                JavaFile.delete(file.makeJavaFile());
            } else if (mFile instanceof AndroidOsLocalFile) {
                AndroidOsLocalFile file = (AndroidOsLocalFile) mFile;
                AndroidOs.delete(file.getPath());
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
            if (mFile instanceof JavaFileLocalFile) {
                JavaFileLocalFile file = (JavaFileLocalFile) mFile;
                JavaFile.rename(file.makeJavaFile(), mNewName);
            } else if (mFile instanceof AndroidOsLocalFile) {
                AndroidOsLocalFile file = (AndroidOsLocalFile) mFile;
                AndroidOs.rename(file.getPath(), mNewName);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }
}
