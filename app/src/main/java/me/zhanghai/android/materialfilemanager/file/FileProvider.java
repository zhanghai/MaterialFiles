/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

import me.zhanghai.android.materialfilemanager.AppApplication;
import me.zhanghai.android.materialfilemanager.BuildConfig;

public class FileProvider extends android.support.v4.content.FileProvider {

    public static Uri getUriForFile(File file) {
        return getUriForFile(AppApplication.getInstance(), BuildConfig.FILE_PROVIDIER_AUTHORITY,
                file);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return MimeTypes.getMimeType(uri);
    }
}
