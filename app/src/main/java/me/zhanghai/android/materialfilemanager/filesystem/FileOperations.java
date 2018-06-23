/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

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
}
