/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Time
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/* @see com.android.documentsui.base.Shared#formatTime(Context, long) */
@Suppress("DEPRECATION")
fun Instant.formatShort(context: Context): String {
    val time = toEpochMilli()
    val then = Time().apply { set(time) }
    val now = Time().apply { setToNow() }
    val flags = DateUtils.FORMAT_NO_NOON or DateUtils.FORMAT_NO_MIDNIGHT or
        DateUtils.FORMAT_ABBREV_ALL or when {
            then.year != now.year -> DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE
            then.yearDay != now.yearDay -> DateUtils.FORMAT_SHOW_DATE
            else -> DateUtils.FORMAT_SHOW_TIME
        }
    return DateUtils.formatDateTime(context, time, flags)
}

fun Instant.formatLong(): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(this)
