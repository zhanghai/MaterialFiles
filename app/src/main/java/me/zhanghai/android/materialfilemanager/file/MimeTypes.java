/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

public class MimeTypes {

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    public static int getIconRes(String mimeType) {
        return MimeTypeIcons.get(mimeType);
    }
}
