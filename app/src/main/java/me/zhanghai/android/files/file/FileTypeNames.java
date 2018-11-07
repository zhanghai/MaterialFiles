/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.MapBuilder;

public class FileTypeNames {

    /** @see MimeTypes#sPosixFileTypeToMimeTypeMap */
    private static final Map<String, Integer> sPosixFileTypeToTypeNameResMap =
            MapBuilder.<String, Integer>newHashMap()
                    .put("inode/chardevice", R.string.file_type_name_posix_character_device)
                    .put("inode/blockdevice", R.string.file_type_name_posix_block_device)
                    .put("inode/fifo", R.string.file_type_name_posix_fifo)
                    .put("inode/symlink", R.string.file_type_name_posix_symbolic_link)
                    .put("inode/socket", R.string.file_type_name_posix_socket)
                    .buildUnmodifiable();

    private FileTypeNames() {}

    @NonNull
    public static String getTypeName(@NonNull String mimeType, @NonNull String extension,
                                     @NonNull Context context) {
        int typeRes = getTypeNameRes(mimeType, !TextUtils.isEmpty(extension));
        extension = extension.toUpperCase(Locale.US);
        return context.getString(typeRes, extension);
    }

    private static int getTypeNameRes(@NonNull String mimeType, boolean hasExtension) {
        Integer posixTypeNameRes = sPosixFileTypeToTypeNameResMap.get(mimeType);
        if (posixTypeNameRes != null) {
            return posixTypeNameRes;
        }
        int iconRes = MimeTypes.getIconRes(mimeType);
        switch (iconRes) {
            case MimeTypeIcons.Icons.APK:
                return R.string.file_type_name_apk;
            case MimeTypeIcons.Icons.ARCHIVE:
                return R.string.file_type_name_archive;
            case MimeTypeIcons.Icons.AUDIO:
                return R.string.file_type_name_audio;
            case MimeTypeIcons.Icons.CALENDAR:
                return R.string.file_type_name_calendar;
            case MimeTypeIcons.Icons.CERTIFICATE:
                return R.string.file_type_name_certificate;
            case MimeTypeIcons.Icons.CODE:
                return R.string.file_type_name_code;
            case MimeTypeIcons.Icons.CONTACT:
                return R.string.file_type_name_contact;
            case MimeTypeIcons.Icons.DIRECTORY:
                return R.string.file_type_name_directory;
            case MimeTypeIcons.Icons.DOCUMENT:
                return R.string.file_type_name_document;
            case MimeTypeIcons.Icons.EBOOK:
                return R.string.file_type_name_ebook;
            case MimeTypeIcons.Icons.EMAIL:
                return R.string.file_type_name_email;
            case MimeTypeIcons.Icons.FONT:
                return R.string.file_type_name_font;
            case MimeTypeIcons.Icons.GENERIC:
                if (!hasExtension) {
                    return R.string.file_type_name_unknown;
                }
                return R.string.file_type_name_generic;
            case MimeTypeIcons.Icons.IMAGE:
                return R.string.file_type_name_image;
            case MimeTypeIcons.Icons.PDF:
                return R.string.file_type_name_pdf;
            case MimeTypeIcons.Icons.PRESENTATION:
                return R.string.file_type_name_presentation;
            case MimeTypeIcons.Icons.SPREADSHEET:
                return R.string.file_type_name_spreadsheet;
            case MimeTypeIcons.Icons.TEXT:
                if (TextUtils.equals(mimeType, "text/plain")) {
                    return R.string.file_type_name_text_plain;
                }
                return R.string.file_type_name_text;
            case MimeTypeIcons.Icons.VIDEO:
                return R.string.file_type_name_video;
            case MimeTypeIcons.Icons.WORD:
                return R.string.file_type_name_word;
            case MimeTypeIcons.Icons.EXCEL:
                return R.string.file_type_name_excel;
            case MimeTypeIcons.Icons.POWERPOINT:
                return R.string.file_type_name_powerpoint;
            default:
                throw new IllegalArgumentException("Unknown icon resource: " + iconRes);
        }
    }

    @NonNull
    public static String getBrokenSymbolicLinkTypeName(@NonNull Context context) {
        return context.getString(R.string.file_type_name_posix_symbolic_link_broken);
    }
}
