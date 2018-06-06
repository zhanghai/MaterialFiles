/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.net.Uri;
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

    public static String getMimeType(Uri uri) {
        return getMimeType(uri.toString());
    }

    public static int getIconRes(String mimeType) {
        return MimeTypeIcons.get(mimeType);
    }

    public static boolean supportsThumbnail(String mimeType) {
        return mimeType.startsWith("image/") || mimeType.startsWith("video/")
                || mimeType.equals("application/vnd.android.package-archive");
    }
}
