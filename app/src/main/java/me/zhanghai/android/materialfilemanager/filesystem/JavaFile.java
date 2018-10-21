/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import me.zhanghai.android.materialfilemanager.R;

public class JavaFile {

    public static long getFreeSpace(@NonNull String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    public static long getTotalSpace(@NonNull String path) {
        File file = new File(path);
        return file.getTotalSpace();
    }

    @NonNull
    public static List<String> getChildren(@NonNull String path) throws FileSystemException {
        File file = new File(path);
        String[] children = file.list();
        if (children == null) {
            throw new FileSystemException(R.string.file_list_error_directory);
        }
        return Arrays.asList(children);
    }
}
