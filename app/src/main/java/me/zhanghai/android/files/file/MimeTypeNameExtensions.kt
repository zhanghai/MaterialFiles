/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.Context
import me.zhanghai.android.files.R
import java.util.Locale

fun MimeType.getName(extension: String, context: Context): String {
    val nameRes = specialPosixFileTypeToNameResMap[this]
        ?: icon.getNameRes(this, extension.isNotEmpty())
    return context.getString(nameRes, extension.uppercase())
}

/**
 *  @see specialPosixFileTypeToMimeTypeMap
 */
private val specialPosixFileTypeToNameResMap = mapOf(
    "inode/chardevice" to R.string.file_type_name_posix_character_device,
    "inode/blockdevice" to R.string.file_type_name_posix_block_device,
    "inode/fifo" to R.string.file_type_name_posix_fifo,
    "inode/symlink" to R.string.file_type_name_posix_symbolic_link,
    "inode/socket" to R.string.file_type_name_posix_socket
).mapKeys { it.key.asMimeType() }

private fun MimeTypeIcon.getNameRes(mimeType: MimeType, hasExtension: Boolean): Int =
    when (this) {
        MimeTypeIcon.APK -> R.string.file_type_name_apk
        MimeTypeIcon.ARCHIVE -> R.string.file_type_name_archive
        MimeTypeIcon.AUDIO -> R.string.file_type_name_audio
        MimeTypeIcon.CALENDAR -> R.string.file_type_name_calendar
        MimeTypeIcon.CERTIFICATE -> R.string.file_type_name_certificate
        MimeTypeIcon.CODE -> R.string.file_type_name_code
        MimeTypeIcon.CONTACT -> R.string.file_type_name_contact
        MimeTypeIcon.DIRECTORY -> R.string.file_type_name_directory
        MimeTypeIcon.DOCUMENT -> R.string.file_type_name_document
        MimeTypeIcon.EBOOK -> R.string.file_type_name_ebook
        MimeTypeIcon.EMAIL -> R.string.file_type_name_email
        MimeTypeIcon.FONT -> R.string.file_type_name_font
        MimeTypeIcon.GENERIC -> {
            if (!hasExtension) R.string.file_type_name_unknown else R.string.file_type_name_generic
        }
        MimeTypeIcon.IMAGE -> R.string.file_type_name_image
        MimeTypeIcon.PDF -> R.string.file_type_name_pdf
        MimeTypeIcon.PRESENTATION -> R.string.file_type_name_presentation
        MimeTypeIcon.SPREADSHEET -> R.string.file_type_name_spreadsheet
        MimeTypeIcon.TEXT ->
            if (mimeType == MimeType.TEXT_PLAIN) {
                R.string.file_type_name_text_plain
            } else {
                R.string.file_type_name_text
            }
        MimeTypeIcon.VIDEO -> R.string.file_type_name_video
        MimeTypeIcon.WORD -> R.string.file_type_name_word
        MimeTypeIcon.EXCEL -> R.string.file_type_name_excel
        MimeTypeIcon.POWERPOINT -> R.string.file_type_name_powerpoint
    }

fun MimeType.Companion.getBrokenSymbolicLinkName(context: Context): String =
    context.getString(R.string.file_type_name_posix_symbolic_link_broken)
