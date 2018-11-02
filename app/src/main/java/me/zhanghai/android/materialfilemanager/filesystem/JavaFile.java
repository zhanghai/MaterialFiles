/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.support.annotation.NonNull;

import java.io.File;

public class JavaFile {

    public static long getFreeSpace(@NonNull String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    public static long getTotalSpace(@NonNull String path) {
        File file = new File(path);
        return file.getTotalSpace();
    }
}
