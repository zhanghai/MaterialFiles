/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import android.os.Build
import androidx.exifinterface.media.ExifInterface
import me.zhanghai.android.files.util.takeIfNotBlank
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

fun ExifInterface.getAttributeNotBlank(tag: String): String? =
    getAttribute(tag)?.takeIfNotBlank()

fun ExifInterface.getAttributeDoubleOrNull(tag: String): Double? =
    getAttributeDouble(tag, Double.NaN).takeIf { !it.isNaN() }

fun ExifInterface.getAttributeIntOrNull(tag: String): Int? =
    getAttributeInt(tag, -1).takeIf { it != -1 || getAttributeInt(tag, 0) == -1 }

val ExifInterface.gpsAltitude: Double?
    get() = getAltitude(Double.NaN).takeIf { !it.isNaN() }

val ExifInterface.dateTimeOriginalCompat: Long?
    get() =
        parseDateTime(
            ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL
        )

private val nonZeroTimeRegex = Regex(".*[1-9].*")
private val primaryDateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }
private val secondaryDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }

/** @see android.media.ExifInterface.parseDateTime */
private fun ExifInterface.parseDateTime(
    dateTimeTag: String,
    offsetTimeTag: String,
    subSecTimeTag: String
): Long? {
    val dateTimeString = getAttributeNotBlank(dateTimeTag)
    if (dateTimeString == null || !dateTimeString.matches(nonZeroTimeRegex)) {
        return null
    }
    val date = primaryDateFormat.parse(dateTimeString, ParsePosition(0))
        ?: secondaryDateFormat.parse(dateTimeString, ParsePosition(0)) ?: return null
    val offsetTimeString = getAttributeNotBlank(offsetTimeTag)
    if (offsetTimeString != null) {
        val offsetTime = parseOffsetTime(offsetTimeString) ?: return null
        // We need to subtract the offset from UTC to get time in UTC from local time.
        date.time = date.time - offsetTime.time
    }
    var time = date.time
    val subSecTimeString = getAttributeNotBlank(subSecTimeTag)
    if (subSecTimeString != null) {
        var subSecTime = subSecTimeString.toLongOrNull()
        if (subSecTime != null) {
            while (subSecTime > 1000) {
                subSecTime /= 10
            }
            time += subSecTime
        }
    }
    return time
}

// X requires API 24+
private val offsetTimeDateFormat =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat("XXX", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    } else {
        null
    }

private fun parseOffsetTime(offsetTimeString: String): Date? {
    if (offsetTimeDateFormat != null) {
        return offsetTimeDateFormat.parse(offsetTimeString, ParsePosition(0))
            // Local epoch with a positive offset from UTC comes before epoch in UTC.
            ?.apply { time = -time }
    } else {
        if (offsetTimeString.length != 6) {
            return null
        }
        val isPositive = when (offsetTimeString[0]) {
            '+' -> true
            '-' -> false
            else -> return null
        }
        val hours = offsetTimeString.substring(1, 3).toLongOrNull() ?: return null
        if (offsetTimeString[3] != ':') {
            return null
        }
        val minutes = offsetTimeString.substring(4, 6).toLongOrNull() ?: return null
        return Duration.ofHours(hours)
            .plusMinutes(minutes)
            .let { if (isPositive) it else it.negated() }
            .toMillis()
            .let { Date(it) }
    }
}

/* @see com.android.providers.media.scan.ModernMediaScanner.parseOptionalDateTaken */
fun ExifInterface.inferDateTimeOriginal(lastModifiedTime: Instant): Instant? {
    val dateTimeOriginal = dateTimeOriginalCompat?.let { Instant.ofEpochMilli(it) } ?: return null
    if (getAttributeNotBlank(ExifInterface.TAG_OFFSET_TIME_ORIGINAL) != null) {
        return dateTimeOriginal
    }
    val gpsDateTime = gpsDateTime?.let { Instant.ofEpochMilli(it) }
    if (gpsDateTime != null) {
        dateTimeOriginal.withTimezoneInferredFrom(gpsDateTime)?.let { return it }
    }
    dateTimeOriginal.withTimezoneInferredFrom(lastModifiedTime)?.let { return it }
    // We don't have any timezone information, pretend that it's in the current timezone which is
    // still better than in UTC.
    return dateTimeOriginal
        .atOffset(ZoneOffset.UTC)
        .toLocalDateTime()
        .atZone(ZoneId.systemDefault())
        .toInstant()
}

private fun Instant.withTimezoneInferredFrom(other: Instant): Instant? {
    val smallestZone = Duration.ofMinutes(15)
    val offset = Duration.between(this, other)
    if (offset.abs() < Duration.ofDays(1)) {
        val smallestZoneMillis = smallestZone.toMillis()
        val rounded = Duration.ofMillis(
            (offset.toMillis().toDouble() / smallestZoneMillis).roundToLong() * smallestZoneMillis
        )
        return this + rounded
    }
    return null
}
