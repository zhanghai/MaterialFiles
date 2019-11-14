/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.os.Build;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java9.util.function.Function;
import me.zhanghai.android.files.compat.MapCompat;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.util.FileNameUtils;
import me.zhanghai.android.files.util.MapBuilder;
import me.zhanghai.android.files.util.SetBuilder;
import me.zhanghai.java.functional.Functional;

// TODO: Use Debian mime.types, as in
//  https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/net/mime.types
public class MimeTypes {

    public static final String ANY_MIME_TYPE = "*/*";
    public static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
    public static final String DIRECTORY_MIME_TYPE = DocumentsContract.Document.MIME_TYPE_DIR;
    public static final String GENERIC_MIME_TYPE = "application/octet-stream";

    // See also https://android.googlesource.com/platform/libcore/+/lollipop-release/luni/src/main/java/libcore/net/MimeUtils.java
    // See also https://android.googlesource.com/platform/libcore/+/pie-release/luni/src/main/java/libcore/net/MimeUtils.java
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
                    // Fixes
                    .put("tgz", "application/x-gtar-compressed") // Was "application/x-gtar"
                    .put("taz", "application/x-gtar-compressed") // Was "application/x-gtar"
                    .put("csv", "text/csv") // Was "text/comma-separated-values"
                    // Addition
                    .put("gz", "application/gzip")
                    .put("cab", "application/vnd.ms-cab-compressed")
                    .put("7z", "application/x-7z-compressed")
                    .put("bz", "application/x-bzip")
                    .put("bz2", "application/x-bzip2")
                    .put("z", "application/x-compress")
                    .put("jar", "application/x-java-archive")
                    .put("lzma", "application/x-lzma")
                    .put("xz", "application/x-xz")
                    .put("m3u", "audio/x-mpegurl")
                    .put("m3u8", "audio/x-mpegurl")
                    .put("p7b", "application/x-pkcs7-certificates")
                    .put("spc", "application/x-pkcs7-certificates")
                    .put("p7c", "application/pkcs7-mime")
                    .put("p7s", "application/pkcs7-signature")
                    .put("es", "application/ecmascript")
                    .put("js", "application/javascript")
                    .put("json", "application/json")
                    .put("ts", "application/typescript") // Clashes with "video/mp2ts"
                    .put("perl", "text/x-perl")
                    .put("pl", "text/x-perl")
                    .put("pm", "text/x-perl")
                    .put("py", "text/x-python")
                    .put("py3", "text/x-python")
                    .put("py3x", "text/x-python")
                    .put("pyx", "text/x-python")
                    .put("wsgi", "text/x-python")
                    .put("rb", "text/ruby")
                    .put("sh", "application/x-sh")
                    .put("yaml", "text/x-yaml")
                    .put("yml", "text/x-yaml")
                    .put("asm", "text/x-asm")
                    .put("s", "text/x-asm")
                    .put("cs", "text/x-csharp")
                    .put("azw", "application/vnd.amazon.ebook")
                    .put("ibooks", "application/x-ibooks+zip")
                    .put("mobi", "application/x-mobipocket-ebook")
                    .put("woff", "font/woff")
                    .put("woff2", "font/woff2")
                    .put("msg", "application/vnd.ms-outlook")
                    .put("eml", "message/rfc822")
                    .put("eot", "application/vnd.ms-fontobject")
                    .put("ttf", "font/ttf")
                    .put("otf", "font/otf")
                    .put("ttc", "font/collection")
                    .put("markdown", "text/markdown")
                    .put("md", "text/markdown")
                    .put("mkd", "text/markdown")
                    .put("conf", "text/plain")
                    .put("ini", "text/plain")
                    .put("list", "text/plain")
                    .put("log", "text/plain")
                    .put("prop", "text/plain")
                    .put("properties", "text/plain")
                    .put("rc", "text/plain")
                    .put("flv", "video/x-flv")
                    .buildUnmodifiable();

    // See also https://developer.gnome.org/shared-mime-info-spec/
    /** @see FileTypeNames#sPosixFileTypeToTypeNameResMap */
    private static final Map<PosixFileType, String> sPosixFileTypeToMimeTypeMap =
            MapBuilder.<PosixFileType, String>newHashMap()
                    .put(PosixFileType.CHARACTER_DEVICE, "inode/chardevice")
                    .put(PosixFileType.BLOCK_DEVICE, "inode/blockdevice")
                    .put(PosixFileType.FIFO, "inode/fifo")
                    .put(PosixFileType.SYMBOLIC_LINK, "inode/symlink")
                    .put(PosixFileType.SOCKET, "inode/socket")
                    .buildUnmodifiable();

    private static final Map<String, String> sMimeTypeToIntentTypeMap =
            MapBuilder.<String, String>newHashMap()
                    // Allows matching "text/*"
                    .put("application/ecmascript", "text/ecmascript")
                    .put("application/javascript", "text/javascript")
                    .put("application/json", "text/json")
                    .put("application/typescript", "text/typescript")
                    .put("application/x-sh", "text/x-shellscript")
                    .put("application/x-shellscript", "text/x-shellscript")
                    // Allows matching generic
                    .put(GENERIC_MIME_TYPE, ANY_MIME_TYPE)
                    .build();

    private static final Set<String> sSupportedArchiveMimeTypes = SetBuilder.<String>newHashSet()
            .add("application/gzip")
            .add("application/java-archive")
            .add("application/rar")
            .add("application/zip")
            .add("application/vnd.android.package-archive")
            .add("application/vnd.debian.binary-package")
            // Requires O and handled in isSupportedArchive.
            //.add("application/x-7z-compressed")
            .add("application/x-bzip2")
            .add("application/x-compress")
            .add("application/x-cpio")
            .add("application/x-deb")
            .add("application/x-debian-package")
            .add("application/x-gtar")
            .add("application/x-gtar-compressed")
            .add("application/x-java-archive")
            .add("application/x-lzma")
            .add("application/x-tar")
            .add("application/x-xz")
            .buildUnmodifiable();

    @NonNull
    public static String getMimeType(@NonNull String path) {
        String extension = FileNameUtils.getExtension(path);
        extension = extension.toLowerCase(Locale.US);
        String mimeType = sExtensionToMimeTypeMap.get(extension);
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        return GENERIC_MIME_TYPE;
    }

    @Nullable
    public static String getPosixMimeType(@NonNull PosixFileType type) {
        return sPosixFileTypeToMimeTypeMap.get(type);
    }

    @NonNull
    public static String getIntentType(@NonNull String mimeType) {
        return MapCompat.getOrDefault(sMimeTypeToIntentTypeMap, mimeType, mimeType);
    }

    public static String getIntentType(@NonNull List<String> mimeTypes) {
        List<MimeTypeInfo> mimeTypeInfos = Functional.map(mimeTypes,
                ((Function<String, String>) MimeTypes::getIntentType).andThen(MimeTypeInfo::parse));
        if (Functional.some(mimeTypeInfos, java9.util.Objects::isNull)) {
            return ANY_MIME_TYPE;
        }
        MimeTypeInfo fullTypeInfo = mimeTypeInfos.get(0);
        if (Functional.every(mimeTypeInfos, mimeTypeInfo -> mimeTypeInfo.matches(fullTypeInfo))) {
            return fullTypeInfo.toString();
        }
        MimeTypeInfo partialTypeInfo = new MimeTypeInfo(fullTypeInfo.type, "*", null);
        if (Functional.every(mimeTypeInfos, mimeTypeInfo ->
                mimeTypeInfo.matches(partialTypeInfo))) {
            return partialTypeInfo.toString();
        }
        return ANY_MIME_TYPE;
    }

    public static int getIconRes(@NonNull String mimeType) {
        return MimeTypeIcons.get(mimeType);
    }

    public static boolean supportsThumbnail(@NonNull String mimeType) {
        return isImage(mimeType) || isMedia(mimeType) || Objects.equals(mimeType,
                "application/vnd.android.package-archive");
    }

    public static boolean isApk(@NonNull String mimeType) {
        return Objects.equals(mimeType, APK_MIME_TYPE);
    }

    public static boolean isSupportedArchive(@NonNull String mimeType) {
        return sSupportedArchiveMimeTypes.contains(mimeType)
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        && Objects.equals(mimeType, "application/x-7z-compressed"));
    }

    public static boolean isImage(@NonNull String mimeType) {
        return getIconRes(mimeType) == MimeTypeIcons.Icons.IMAGE;
    }

    public static boolean isMedia(@NonNull String mimeType) {
        int icon = getIconRes(mimeType);
        return icon == MimeTypeIcons.Icons.AUDIO || icon == MimeTypeIcons.Icons.VIDEO;
    }

    public static boolean matches(@NonNull String mimeTypeSpec, @NonNull String mimeType) {
        MimeTypeInfo mimeTypeSpecInfo = MimeTypeInfo.parse(mimeTypeSpec);
        if (mimeTypeSpecInfo == null) {
            return false;
        }
        MimeTypeInfo mimeTypeInfo = MimeTypeInfo.parse(mimeType);
        if (mimeTypeInfo == null) {
            return false;
        }
        return mimeTypeInfo.matches(mimeTypeSpecInfo);
    }
}
