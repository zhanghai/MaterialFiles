/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import java.io.IOException;

import me.zhanghai.android.materialfilemanager.R;

public class FileOperations {

    public static void rename(File file, String name) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            java.io.File javaFile = file.makeJavaFile();
            java.io.File newJavaFile = new java.io.File(javaFile.getParent(), name);
            boolean result = javaFile.renameTo(newJavaFile);
            if (!result) {
                throw new FileSystemException(R.string.file_rename_error);
            }
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
    }

    public static void createFile(File file) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            java.io.File javaFile = file.makeJavaFile();
            try {
                boolean result = javaFile.createNewFile();
                if (!result) {
                    throw new FileSystemException(
                            R.string.file_create_file_error_already_exists);
                }
            } catch (IOException e) {
                throw new FileSystemException(R.string.file_create_file_error, e);
            }
        }
    }

    public static void createDirectory(File file) throws FileSystemException {
        if (file instanceof JavaLocalFile) {
            java.io.File javaFile = file.makeJavaFile();
            boolean result = javaFile.mkdir();
            if (!result) {
                throw new FileSystemException(R.string.file_create_directory_error);
            }
        }
    }
}
