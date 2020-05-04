/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import androidx.exifinterface.media.ExifInterface
import me.zhanghai.android.files.util.takeIfNotBlank
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.text.ParsePosition
import java.text.SimpleDateFormat
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

val ExifInterface.dateTimeOriginalCompat: Long
    get() =
        parseDateTime(
            ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL
        )

private val nonZeroTimeRegex = Regex(".*[1-9].*")
private val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }
private val dateFormatWithTimezone = SimpleDateFormat("yyyy:MM:dd HH:mm:ss XXX", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }

/** @see android.media.ExifInterface.parseDateTime */
private fun ExifInterface.parseDateTime(
    dateTimeTag: String,
    offsetTimeTag: String,
    subSecTimeTag: String
): Long {
    val dateTimeString = getAttributeNotBlank(dateTimeTag)
    if (dateTimeString == null || !dateTimeString.matches(nonZeroTimeRegex)) {
        return -1
    }
    val offsetTimeString = getAttributeNotBlank(offsetTimeTag)
    val date = if (offsetTimeString != null) {
        dateFormatWithTimezone.parse("$dateTimeString $offsetTimeString", ParsePosition(0))
    } else {
        dateFormat.parse(dateTimeString, ParsePosition(0))
    } ?: return -1
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

/* @see com.android.providers.media.scan.ModernMediaScanner.parseOptionalDateTaken */
fun ExifInterface.inferDateTimeOriginal(lastModifiedTime: Instant): Instant? {
    val dateTimeOriginal = dateTimeOriginalCompat.takeIf { it != -1L }
        ?.let { Instant.ofEpochMilli(it) } ?: return null
    if (getAttributeNotBlank(ExifInterface.TAG_OFFSET_TIME_ORIGINAL) != null) {
        return dateTimeOriginal
    }
    val gpsDateTime = gpsDateTime.takeIf { it != -1L }?.let { Instant.ofEpochMilli(it) }
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
