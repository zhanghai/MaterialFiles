/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import android.annotation.SuppressLint
import androidx.exifinterface.media.ExifInterface
import me.zhanghai.android.files.util.takeIfNotBlank
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import kotlin.math.roundToLong

fun ExifInterface.getAttributeNotBlank(tag: String): String? =
    getAttribute(tag)?.takeIfNotBlank()

fun ExifInterface.getAttributeDoubleOrNull(tag: String): Double? =
    getAttributeDouble(tag, Double.NaN).takeIf { !it.isNaN() }

fun ExifInterface.getAttributeIntOrNull(tag: String): Int? =
    getAttributeInt(tag, -1).takeIf { it != -1 || getAttributeInt(tag, 0) == -1 }

val ExifInterface.gpsAltitude: Double?
    get() = getAltitude(Double.NaN).takeIf { !it.isNaN() }

/* @see com.android.providers.media.scan.ModernMediaScanner.parseOptionalDateTaken */
@SuppressLint("RestrictedApi")
fun ExifInterface.inferDateTimeOriginal(lastModifiedTime: Instant): Instant? {
    val dateTimeOriginal = dateTimeOriginal?.let { Instant.ofEpochMilli(it) } ?: return null
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
