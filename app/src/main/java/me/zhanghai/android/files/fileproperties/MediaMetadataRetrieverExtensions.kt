/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import android.media.MediaMetadataRetriever
import me.zhanghai.android.files.util.takeIfNotBlank
import org.threeten.bp.Instant
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

fun MediaMetadataRetriever.extractMetadataNotBlank(keyCode: Int): String? =
    extractMetadata(keyCode)?.takeIfNotBlank()

private val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }

// @see com.android.providers.media.scan.ModernMediaScanner.parseOptionalDate
val MediaMetadataRetriever.date: Instant?
    get() {
        val date = extractMetadataNotBlank(MediaMetadataRetriever.METADATA_KEY_DATE) ?: return null
        return dateFormat.parse(date, ParsePosition(0))?.time?.let { Instant.ofEpochMilli(it) }
    }

// @see android.media.cts.MediaRecorderTest.checkLocationInFile
val MediaMetadataRetriever.location: Pair<Float, Float>?
    get() {
        var location = extractMetadataNotBlank(MediaMetadataRetriever.METADATA_KEY_LOCATION)
            ?: return null
        if (location.endsWith('/')) {
            location = location.dropLast(1)
        }
        val index = max(location.lastIndexOf('+'), location.lastIndexOf('-'))
        if (index <= 0) {
            return null
        }
        val latitude = location.substring(0, index).toFloatOrNull() ?: return null
        val longitude = location.substring(index).toFloatOrNull() ?: return null
        return latitude to longitude
    }
