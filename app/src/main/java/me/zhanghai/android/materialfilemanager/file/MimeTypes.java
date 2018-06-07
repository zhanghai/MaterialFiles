/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.util.Locale;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.util.MapBuilder;

public class MimeTypes {

    // See also https://android.googlesource.com/platform/libcore/+/lollipop-release/luni/src/main/java/libcore/net/MimeUtils.java
    // See also https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/net/MimeUtils.java
    // See also http://www.iana.org/assignments/media-types/media-types.xhtml
    // See also /usr/share/mime/packages/freedesktop.org.xml
    private static final Map<String, String> sExtensionToMimeTypeMap =
            MapBuilder.<String, String>newHashMap()
                    // Compatibility (starting from L), in the order they appear in Android source.
                    .put("epub", "application/epub+zip")
                    .put("ogx", "application/ogg")
                    .put("odp", "application/vnd.oasis.opendocument.presentation")
                    .put("otp", "application/vnd.oasis.opendocument.presentation-template")
                    .put("yt", "application/vnd.youtube.yt")
                    .put("hwp", "application/x-hwp")
                    .put("3gpp", "video/3gpp")
                    .put("3gp", "video/3gpp")
                    .put("3gpp2", "video/3gpp2")
                    .put("3g2", "video/3gpp2")
                    .put("oga", "audio/ogg")
                    .put("ogg", "audio/ogg")
                    .put("spx", "audio/ogg")
                    .put("dng", "image/x-adobe-dng")
                    .put("cr2", "image/x-canon-cr2")
                    .put("raf", "image/x-fuji-raf")
                    .put("nef", "image/x-nikon-nef")
                    .put("nrw", "image/x-nikon-nrw")
                    .put("orf", "image/x-olympus-orf")
                    .put("rw2", "image/x-panasonic-rw2")
                    .put("pef", "image/x-pentax-pef")
                    .put("srw", "image/x-samsung-srw")
                    .put("arw", "image/x-sony-arw")
                    .put("ogv", "video/ogg")
                    // Addition
                    .put("gz", "application/gzip")
                    .put("7z", "application/x-7z-compressed")
                    .put("bz", "application/x-bzip")
                    .put("bz2", "application/x-bzip2")
                    .put("jar", "application/x-java-archive")
                    .put("azw", "application/vnd.amazon.ebook")
                    .put("mobi", "application/x-mobipocket-ebook")
                    .put("p7b", "application/x-pkcs7-certificates")
                    .put("spc", "application/x-pkcs7-certificates")
                    .put("p7c", "application/pkcs7-mime")
                    .put("p7s", "application/pkcs7-signature")
                    .put("es", "application/ecmascript")
                    .put("js", "application/javascript")
                    .put("json", "application/json")
                    .put("ts", "application/typescript") // Clashes with "video/mp2ts"
                    .put("sh", "application/x-sh")
                    .put("yaml", "application/x-yaml")
                    .put("yml", "application/x-yaml")
                    .put("woff", "font/woff")
                    .put("woff2", "font/woff2")
                    .put("eot", "application/vnd.ms-fontobject")
                    .put("ttf", "font/ttf")
                    .put("ttc", "font/collection")
                    .put("flv", "video/x-flv")

                    .buildUnmodifiable();

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        extension = extension.toLowerCase(Locale.US);
        String mimeType = sExtensionToMimeTypeMap.get(extension);
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
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
