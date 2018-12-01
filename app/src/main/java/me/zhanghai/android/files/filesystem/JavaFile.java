/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import java.io.File;

import androidx.annotation.NonNull;

public class JavaFile {

    public static boolean isDirectory(@NonNull String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public static long getFreeSpace(@NonNull String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    public static long getTotalSpace(@NonNull String path) {
        File file = new File(path);
        return file.getTotalSpace();
    }
}
