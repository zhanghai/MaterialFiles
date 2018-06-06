/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.provider.DocumentsContract;

import java.util.Map;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.util.MapBuilder;

// See also https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/util/MimeIconUtils.java
// See also http://www.iana.org/assignments/media-types/media-types.xhtml
// See also https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
class MimeTypeIcons {

    private interface Icon {
        int APK = R.drawable.file_apk_icon_white_40dp;
        int ARCHIVE = R.drawable.file_archive_icon_white_40dp;
        int AUDIO = R.drawable.file_audio_icon_white_40dp;
        int BOOK = R.drawable.file_book_icon_white_40dp;
        int CALENDAR = R.drawable.file_calendar_icon_white_40dp;
        int CERTIFICATE = R.drawable.file_certificate_icon_white_40dp;
        int CODE = R.drawable.file_code_icon_white_40dp;
        int CONTACT = R.drawable.file_contact_icon_white_40dp;
        int DIRECTORY = R.drawable.directory_icon_white_40dp;
        int DOCUMENT = R.drawable.file_document_icon_white_40dp;
        int FONT = R.drawable.file_font_icon_white_40dp;
        int GENERIC = R.drawable.file_icon_white_40dp;
        int IMAGE = R.drawable.file_image_icon_white_40dp;
        int PDF = R.drawable.file_pdf_icon_white_40dp;
        int PRESENTATION = R.drawable.file_presentation_icon_white_40dp;
        int SPREADSHEET = R.drawable.file_spreadsheet_icon_white_40dp;
        int TEXT = R.drawable.file_text_icon_white_40dp;
        int VIDEO = R.drawable.file_video_icon_white_40dp;
    }

    private static final Map<String, Integer> sTypeToIconMap =
            MapBuilder.<String, Integer>newHashMap()
                    .put("audio", Icon.AUDIO)
                    .put("font", Icon.FONT)
                    .put("image", Icon.IMAGE)
                    .put("text", Icon.TEXT)
                    .put("video", Icon.VIDEO)
                    .buildUnmodifiable();

    private static final Map<String, Integer> sMimeTypeToIconMap =
            MapBuilder.<String, Integer>newHashMap()

                    .put("application/vnd.android.package-archive", Icon.APK)

                    .put("application/gzip", Icon.ARCHIVE)
                    .put("application/java-archive", Icon.ARCHIVE)
                    .put("application/mac-binhex40", Icon.ARCHIVE)
                    .put("application/rar", Icon.ARCHIVE)
                    .put("application/zip", Icon.ARCHIVE)
                    .put("application/x-7z-compressed", Icon.ARCHIVE)
                    .put("application/x-apple-diskimage", Icon.ARCHIVE)
                    .put("application/x-bzip", Icon.ARCHIVE)
                    .put("application/x-bzip2", Icon.ARCHIVE)
                    .put("application/x-deb", Icon.ARCHIVE)
                    .put("application/x-debian-package", Icon.ARCHIVE)
                    .put("application/x-gtar", Icon.ARCHIVE)
                    .put("application/x-iso9660-image", Icon.ARCHIVE)
                    .put("application/x-lha", Icon.ARCHIVE)
                    .put("application/x-lzh", Icon.ARCHIVE)
                    .put("application/x-lzx", Icon.ARCHIVE)
                    .put("application/x-rar-compressed", Icon.ARCHIVE)
                    .put("application/x-stuffit", Icon.ARCHIVE)
                    .put("application/x-tar", Icon.ARCHIVE)
                    .put("application/x-webarchive", Icon.ARCHIVE)
                    .put("application/x-webarchive-xml", Icon.ARCHIVE)


                    .put("application/x-flac", Icon.AUDIO)

                    .put("application/epub+zip", Icon.BOOK)
                    .put("application/vnd.amazon.ebook", Icon.BOOK)

                    .put("text/calendar", Icon.CALENDAR)
                    .put("text/x-vcalendar", Icon.CALENDAR)

                    .put("application/pgp-keys", Icon.CERTIFICATE)
                    .put("application/pgp-signature", Icon.CERTIFICATE)
                    .put("application/x-pkcs12", Icon.CERTIFICATE)
                    .put("application/x-pkcs7-certificates", Icon.CERTIFICATE)
                    .put("application/x-pkcs7-certreqresp", Icon.CERTIFICATE)
                    .put("application/x-pkcs7-crl", Icon.CERTIFICATE)
                    .put("application/x-pkcs7-mime", Icon.CERTIFICATE)
                    .put("application/x-pkcs7-signature", Icon.CERTIFICATE)
                    .put("application/x-x509-ca-cert", Icon.CERTIFICATE)
                    .put("application/x-x509-user-cert", Icon.CERTIFICATE)

                    .put("application/ecmascript", Icon.CODE)
                    .put("application/javascript", Icon.CODE)
                    .put("application/json", Icon.CODE)
                    .put("application/xml", Icon.CODE)
                    .put("application/x-csh", Icon.CODE)
                    .put("application/x-javascript", Icon.CODE)
                    .put("application/x-latex", Icon.CODE)
                    .put("application/x-sh", Icon.CODE)
                    .put("application/x-texinfo", Icon.CODE)
                    .put("application/x-yaml", Icon.CODE)
                    .put("text/css", Icon.CODE)
                    .put("text/html", Icon.CODE)
                    .put("text/javascript", Icon.CODE)
                    .put("text/xml", Icon.CODE)
                    .put("text/x-chdr", Icon.CODE)
                    .put("text/x-csh", Icon.CODE)
                    .put("text/x-csrc", Icon.CODE)
                    .put("text/x-c++hdr", Icon.CODE)
                    .put("text/x-c++src", Icon.CODE)
                    .put("text/x-dsrc", Icon.CODE)
                    .put("text/x-haskell", Icon.CODE)
                    .put("text/x-java", Icon.CODE)
                    .put("text/x-literate-haskell", Icon.CODE)
                    .put("text/x-pascal", Icon.CODE)
                    .put("text/x-shellscript", Icon.CODE)
                    .put("text/x-tcl", Icon.CODE)
                    .put("text/x-tex", Icon.CODE)
                    .put("text/x-yaml", Icon.CODE)

                    .put("text/vcard", Icon.CONTACT)
                    .put("text/x-vcard", Icon.CONTACT)

                    .put("inode/directory", Icon.DIRECTORY)
                    .put(DocumentsContract.Document.MIME_TYPE_DIR, Icon.DIRECTORY)

                    .put("application/msword", Icon.DOCUMENT)
                    .put("application/rtf", Icon.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text", Icon.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-master", Icon.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-template", Icon.DOCUMENT)
                    .put("application/vnd.oasis.opendocument.text-web", Icon.DOCUMENT)
                    .put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Icon.DOCUMENT)
                    .put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", Icon.DOCUMENT)
                    .put("application/vnd.stardivision.writer", Icon.DOCUMENT)
                    .put("application/vnd.stardivision.writer-global", Icon.DOCUMENT)
                    .put("application/vnd.sun.xml.writer", Icon.DOCUMENT)
                    .put("application/vnd.sun.xml.writer.global", Icon.DOCUMENT)
                    .put("application/vnd.sun.xml.writer.template", Icon.DOCUMENT)
                    .put("application/x-abiword", Icon.DOCUMENT)
                    .put("application/x-kword", Icon.DOCUMENT)

                    .put("application/font-woff", Icon.FONT)
                    .put("application/vnd.ms-fontobject", Icon.FONT)
                    .put("application/x-font", Icon.FONT)
                    .put("application/x-font-ttf", Icon.FONT)
                    .put("application/x-font-woff", Icon.FONT)

                    .put("application/vnd.oasis.opendocument.graphics", Icon.IMAGE)
                    .put("application/vnd.oasis.opendocument.graphics-template", Icon.IMAGE)
                    .put("application/vnd.oasis.opendocument.image", Icon.IMAGE)
                    .put("application/vnd.stardivision.draw", Icon.IMAGE)
                    .put("application/vnd.sun.xml.draw", Icon.IMAGE)
                    .put("application/vnd.sun.xml.draw.template", Icon.IMAGE)
                    .put("application/vnd.visio", Icon.IMAGE)

                    .put("application/pdf", Icon.PDF)

                    .put("application/vnd.ms-powerpoint", Icon.PRESENTATION)
                    .put("application/vnd.oasis.opendocument.presentation", Icon.PRESENTATION)
                    .put("application/vnd.oasis.opendocument.presentation-template", Icon.PRESENTATION)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.presentation", Icon.PRESENTATION)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", Icon.PRESENTATION)
                    .put("application/vnd.openxmlformats-officedocument.presentationml.template", Icon.PRESENTATION)
                    .put("application/vnd.stardivision.impress", Icon.PRESENTATION)
                    .put("application/vnd.sun.xml.impress", Icon.PRESENTATION)
                    .put("application/vnd.sun.xml.impress.template", Icon.PRESENTATION)
                    .put("application/x-kpresenter", Icon.PRESENTATION)

                    .put("application/vnd.ms-excel", Icon.SPREADSHEET)
                    .put("application/vnd.oasis.opendocument.spreadsheet", Icon.SPREADSHEET)
                    .put("application/vnd.oasis.opendocument.spreadsheet-template", Icon.SPREADSHEET)
                    .put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Icon.SPREADSHEET)
                    .put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", Icon.SPREADSHEET)
                    .put("application/vnd.stardivision.calc", Icon.SPREADSHEET)
                    .put("application/vnd.sun.xml.calc", Icon.SPREADSHEET)
                    .put("application/vnd.sun.xml.calc.template", Icon.SPREADSHEET)
                    .put("application/x-kspread", Icon.SPREADSHEET)

                    .put("application/ecmascript", Icon.TEXT)
                    .put("application/javascript", Icon.TEXT)
                    .put("application/json", Icon.TEXT)
                    .put("application/typescript", Icon.TEXT)

                    .put("application/ogg", Icon.VIDEO)
                    .put("application/x-quicktimeplayer", Icon.VIDEO)
                    .put("application/x-shockwave-flash", Icon.VIDEO)

                    .buildUnmodifiable();

    private static final Map<String, Integer> sSuffixToIconMap =
            MapBuilder.<String, Integer>newHashMap()
                    .put("json", Icon.CODE)
                    .put("xml", Icon.CODE)
                    .put("zip", Icon.ARCHIVE)
                    .buildUnmodifiable();

    private MimeTypeIcons() {}

    public static int get(String mimeType) {
        Integer icon;
        int firstSlashIndex = mimeType.indexOf('/');
        if (firstSlashIndex != -1) {
            String type = mimeType.substring(0, mimeType.indexOf('/'));
            icon = sTypeToIconMap.get(type);
            if (icon != null) {
                return icon;
            }
        }
        icon = sMimeTypeToIconMap.get(mimeType);
        if (icon != null) {
            return icon;
        }
        int lastPlusIndex = mimeType.lastIndexOf('+');
        if (lastPlusIndex != -1 && lastPlusIndex + 1 < mimeType.length()) {
            String suffix = mimeType.substring(lastPlusIndex + 1);
            icon = sSuffixToIconMap.get(suffix);
            if (icon != null) {
                return icon;
            }
        }
        return Icon.GENERIC;
    }
}
