/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.util.Map;

import me.zhanghai.android.materialfilemanager.util.MapBuilder;

public class MimeTypes {

    private static final Map<String, String> sExtensionToMimeTypeMap =
            MapBuilder.<String, String>newHashMap()
                    .put("json", "application/json")
                    .buildUnmodifiable();

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        mimeType = sExtensionToMimeTypeMap.get(extension);
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        return "application/octet-stream";
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
