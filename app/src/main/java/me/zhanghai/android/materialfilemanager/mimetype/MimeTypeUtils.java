/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.mimetype;

import android.webkit.MimeTypeMap;

public class MimeTypeUtils {

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static int getIconRes(String mimeType) {
        return MimeTypeIcons.get(mimeType);
    }
}
