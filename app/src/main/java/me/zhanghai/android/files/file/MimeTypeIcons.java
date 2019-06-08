/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.text.TextUtils;

import java.util.Map;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.MapBuilder;

// See also https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/util/MimeIconUtils.java
// See also https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
// See also http://www.iana.org/assignments/media-types/media-types.xhtml
// See also /usr/share/mime/packages/freedesktop.org.xml
class MimeTypeIcons {

    interface Icons {
        int APK = R.drawable.file_apk_icon_40dp;
        int ARCHIVE = R.drawable.file_archive_icon_40dp;
        int AUDIO = R.drawable.file_audio_icon_40dp;
        int CALENDAR = R.drawable.file_calendar_icon_40dp;
        int CERTIFICATE = R.drawable.file_certificate_icon_40dp;
        int CODE = R.drawable.file_code_icon_40dp;
        int CONTACT = R.drawable.file_contact_icon_40dp;
        int DIRECTORY = R.drawable.directory_icon_40dp;
        int DOCUMENT = R.drawable.file_document_icon_40dp;
        int EBOOK = R.drawable.file_ebook_icon_40dp;
        int EMAIL = R.drawable.file_email_icon_40dp;
        int FONT = R.drawable.file_font_icon_40dp;
        int GENERIC = R.drawable.file_icon_40dp;
        int IMAGE = R.drawable.file_image_icon_40dp;
        int PDF = R.drawable.file_pdf_icon_40dp;
        int PRESENTATION = R.drawable.file_presentation_icon_40dp;
        int SPREADSHEET = R.drawable.file_spreadsheet_icon_40dp;
        int TEXT = R.drawable.file_text_icon_40dp;
        int VIDEO = R.drawable.file_video_icon_40dp;
        int WORD = R.drawable.file_word_icon_40dp;
        int EXCEL = R.drawable.file_excel_icon_40dp;
        int POWERPOINT = R.drawable.file_powerpoint_icon_40dp;
    }

    private static final Map<String, Integer> sMimeTypeToIconMap =
            MapBuilder.<String, Integer>newHashMap()

                    .put("application/vnd.android.package-archive", Icons.APK)

                    .put("application/gzip", Icons.ARCHIVE)
                    // Not in IANA list, but Mozilla and Wikipedia say so.
                    .put("application/java-archive", Icons.ARCHIVE)
                    .put("application/mac-binhex40", Icons.ARCHIVE)
                    // Not in IANA list, but AOSP MimeUtils says so.
                    .put("application/rar", Icons.ARCHIVE)
                    .put("application/zip", Icons.ARCHIVE)
                    .put("application/vnd.debian.binary-package", Icons.ARCHIVE)
                    .put("application/vnd.ms-cab-compressed", Icons.ARCHIVE)
                    .put("application/vnd.rar", Icons.ARCHIVE)
                    .put("application/x-7z-compressed", Icons.ARCHIVE)
                    .put("application/x-apple-diskimage", Icons.ARCHIVE)
                    .put("application/x-bzip", Icons.ARCHIVE)
                    .put("application/x-bzip2", Icons.ARCHIVE)
                    .put("application/x-compress", Icons.ARCHIVE)
                    .put("application/x-cpio", Icons.ARCHIVE)
                    .put("application/x-deb", Icons.ARCHIVE)
                    .put("application/x-debian-package", Icons.ARCHIVE)
                    .put("application/x-gtar", Icons.ARCHIVE)
                    .put("application/x-gtar-compressed", Icons.ARCHIVE)
                    .put("application/x-iso9660-image", Icons.ARCHIVE)
                    .put("application/x-java-archive", Icons.ARCHIVE)
                    .put("application/x-lha", Icons.ARCHIVE)
                    .put("application/x-lzh", Icons.ARCHIVE)
                    .put("application/x-lzma", Icons.ARCHIVE)
                    .put("application/x-lzx", Icons.ARCHIVE)
                    .put("application/x-rar-compressed", Icons.ARCHIVE)
                    .put("application/x-stuffit", Icons.ARCHIVE)
                    .put("application/x-tar", Icons.ARCHIVE)
                    .put("application/x-webarchive", Icons.ARCHIVE)
                    .put("application/x-webarchive-xml", Icons.ARCHIVE)
                    .put("application/x-xz", Icons.ARCHIVE)

                    .put("application/ogg", Icons.AUDIO)
                    .put("application/x-flac", Icons.AUDIO)

                    .put("text/calendar", Icons.CALENDAR)
                    .put("text/x-vcalendar", Icons.CALENDAR)

                    .put("application/pgp-keys", Icons.CERTIFICATE)
                    .put("application/pgp-signature", Icons.CERTIFICATE)
                    .put("application/x-pkcs12", Icons.CERTIFICATE)
                    .put("application/x-pkcs7-certificates", Icons.CERTIFICATE)
                    .put("application/x-pkcs7-certreqresp", Icons.CERTIFICATE)
                    .put("application/x-pkcs7-crl", Icons.CERTIFICATE)
                    .put("application/x-pkcs7-mime", Icons.CERTIFICATE)
                    .put("application/x-pkcs7-signature", Icons.CERTIFICATE)
                    .put("application/x-x509-ca-cert", Icons.CERTIFICATE)
                    .put("application/x-x509-server-cert", Icons.CERTIFICATE)
                    .put("application/x-x509-user-cert", Icons.CERTIFICATE)

                    .put("application/ecmascript", Icons.CODE)
                    .put("application/javascript", Icons.CODE)
                    .put("application/json", Icons.CODE)
                    .put("application/typescript", Icons.CODE)
                    .put("application/xml", Icons.CODE)
                    .put("application/x-csh", Icons.CODE)
                    .put("application/x-javascript", Icons.CODE)
                    .put("application/x-latex", Icons.CODE)
                    .put("application/x-perl", Icons.CODE)
                    .put("application/x-python", Icons.CODE)
                    .put("application/x-ruby", Icons.CODE)
                    .put("application/x-sh", Icons.CODE)
                    .put("application/x-shellscript", Icons.CODE)
                    .put("application/x-texinfo", Icons.CODE)
                    .put("application/x-yaml", Icons.CODE)
                    .put("text/css", Icons.CODE)
                    .put("text/html", Icons.CODE)
                    .put("text/javascript", Icons.CODE)
                    .put("text/xml", Icons.CODE)
                    .put("text/x-asm", Icons.CODE)
                    .put("text/x-c++hdr", Icons.CODE)
                    .put("text/x-c++src", Icons.CODE)
                    .put("text/x-chdr", Icons.CODE)
                    .put("text/x-csh", Icons.CODE)
                    .put("text/x-csharp", Icons.CODE)
                    .put("text/x-csrc", Icons.CODE)
                    .put("text/x-dsrc", Icons.CODE)
                    .put("text/x-haskell", Icons.CODE)
                    .put("text/x-java", Icons.CODE)
                    .put("text/x-literate-haskell", Icons.CODE)
                    .put("text/x-pascal", Icons.CODE)
                    .put("text/x-perl", Icons.CODE)
                    .put("text/x-python", Icons.CODE)
                    .put("text/x-ruby", Icons.CODE)
                    .put("text/x-shellscript", Icons.CODE)
                    .put("text/x-tcl", Icons.CODE)
                    .put("text/x-tex", Icons.CODE)
                    .put("text/x-yaml", Icons.CODE)

                    .put("text/vcard", Icons.CONTACT)
                    .put("text/x-vcard", Icons.CONTACT)

                    .put("inode/directory", Icons.DIRECTORY)
                    .put(MimeTypes.DIRECTORY_MIME_TYPE, Icons.DIRECTORY)

                    .put("application/rtf", Icons.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text", Icons.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-master", Icons.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-template", Icons.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-web", Icons.DOCUMENT)
                    .put("application/vnd.stardivision.writer", Icons.DOCUMENT)
                    .put("application/vnd.stardivision.writer-global", Icons.DOCUMENT)
                    .put("application/vnd.sun.xml.writer", Icons.DOCUMENT)
                    .put("application/vnd.sun.xml.writer.global", Icons.DOCUMENT)
                    .put("application/vnd.sun.xml.writer.template", Icons.DOCUMENT)
                    .put("application/x-abiword", Icons.DOCUMENT)
                    .put("application/x-kword", Icons.DOCUMENT)
                    .put("text/rtf", Icons.DOCUMENT)

                    .put("application/epub+zip", Icons.EBOOK)
                    .put("application/vnd.amazon.ebook", Icons.EBOOK)
                    .put("application/x-ibooks+zip", Icons.EBOOK)
                    .put("application/x-mobipocket-ebook", Icons.EBOOK)

                    .put("application/vnd.ms-outlook", Icons.EMAIL)
                    .put("message/rfc822", Icons.EMAIL)

                    .put("application/font-woff", Icons.FONT)
                    .put("application/vnd.ms-fontobject", Icons.FONT)
                    .put("application/x-font", Icons.FONT)
                    .put("application/x-font-ttf", Icons.FONT)
                    .put("application/x-font-woff", Icons.FONT)

                    .put("application/vnd.oasis.opendocument.graphics", Icons.IMAGE)
                    .put("application/vnd.oasis.opendocument.graphics-template", Icons.IMAGE)
                    .put("application/vnd.oasis.opendocument.image", Icons.IMAGE)
                    .put("application/vnd.stardivision.draw", Icons.IMAGE)
                    .put("application/vnd.sun.xml.draw", Icons.IMAGE)
                    .put("application/vnd.sun.xml.draw.template", Icons.IMAGE)
                    .put("application/vnd.visio", Icons.IMAGE)

                    .put("application/pdf", Icons.PDF)

                    .put("application/vnd.oasis.opendocument.presentation", Icons.PRESENTATION)
                    .put("application/vnd.oasis.opendocument.presentation-template", Icons.PRESENTATION)
                    .put("application/vnd.stardivision.impress", Icons.PRESENTATION)
                    .put("application/vnd.sun.xml.impress", Icons.PRESENTATION)
                    .put("application/vnd.sun.xml.impress.template", Icons.PRESENTATION)
                    .put("application/x-kpresenter", Icons.PRESENTATION)

                    .put("application/vnd.oasis.opendocument.spreadsheet", Icons.SPREADSHEET)
                    .put("application/vnd.oasis.opendocument.spreadsheet-template", Icons.SPREADSHEET)
                    .put("application/vnd.stardivision.calc", Icons.SPREADSHEET)
                    .put("application/vnd.sun.xml.calc", Icons.SPREADSHEET)
                    .put("application/vnd.sun.xml.calc.template", Icons.SPREADSHEET)
                    .put("application/x-kspread", Icons.SPREADSHEET)

                    .put("application/x-quicktimeplayer", Icons.VIDEO)
                    .put("application/x-shockwave-flash", Icons.VIDEO)

                    .put("application/msword", Icons.WORD)
                    .put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Icons.WORD)
                    .put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", Icons.WORD)
                    .put("application/vnd.ms-excel", Icons.EXCEL)
                    .put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Icons.EXCEL)
                    .put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", Icons.EXCEL)
                    .put("application/vnd.ms-powerpoint", Icons.POWERPOINT)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.presentation", Icons.POWERPOINT)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", Icons.POWERPOINT)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.template", Icons.POWERPOINT)

                    .buildUnmodifiable();

    private static final Map<String, Integer> sTypeToIconMap =
            MapBuilder.<String, Integer>newHashMap()
                    .put("audio", Icons.AUDIO)
                    .put("font", Icons.FONT)
                    .put("image", Icons.IMAGE)
                    .put("text", Icons.TEXT)
                    .put("video", Icons.VIDEO)
                    .buildUnmodifiable();

    private static final Map<String, Integer> sSuffixToIconMap =
            MapBuilder.<String, Integer>newHashMap()
                    .put("json", Icons.CODE)
                    .put("xml", Icons.CODE)
                    .put("zip", Icons.ARCHIVE)
                    .buildUnmodifiable();

    private MimeTypeIcons() {}

    public static int get(@NonNull String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return Icons.GENERIC;
        }
        Integer icon = sMimeTypeToIconMap.get(mimeType);
        if (icon != null) {
            return icon;
        }
        int firstSlashIndex = mimeType.indexOf('/');
        if (firstSlashIndex != -1) {
            String type = mimeType.substring(0, mimeType.indexOf('/'));
            icon = sTypeToIconMap.get(type);
            if (icon != null) {
                return icon;
            }
        }
        int lastPlusIndex = mimeType.lastIndexOf('+');
        if (lastPlusIndex != -1 && lastPlusIndex + 1 < mimeType.length()) {
            String suffix = mimeType.substring(lastPlusIndex + 1);
            icon = sSuffixToIconMap.get(suffix);
            if (icon != null) {
                return icon;
            }
        }
        return Icons.GENERIC;
    }
}
