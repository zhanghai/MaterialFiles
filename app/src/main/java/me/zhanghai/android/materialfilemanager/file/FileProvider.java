/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.net.Uri;

import java.io.File;

import me.zhanghai.android.materialfilemanager.AppApplication;
import me.zhanghai.android.materialfilemanager.BuildConfig;

public class FileProvider extends android.support.v4.content.FileProvider {

    public static Uri getUriForFile(File file) {
        return getUriForFile(AppApplication.getInstance(), BuildConfig.FILE_PROVIDIER_AUTHORITY,
                file);
    }
}
