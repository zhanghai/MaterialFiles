/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import android.util.Size
import org.threeten.bp.Instant

class ImageInfo(
    val dimensions: Size?,
    val exifInfo: ExifInfo?
)

// @see com.android.documentsui.inspector.MediaView
// @see https://github.com/GNOME/nautilus/blob/c73ad94a72f8e9a989b01858018de74182d17f0e/extensions/image-properties/nautilus-image-properties-page.c#L198
class ExifInfo(
    val dateTimeOriginal: Instant?,
    val gpsCoordinates: Pair<Double, Double>?,
    val gpsAltitude: Double?,
    val make: String?,
    val model: String?,
    val fNumber: Double?,
    val shutterSpeedValue: Double?,
    val focalLength: Double?,
    val photographicSensitivity: Int?,
    val software: String?,
    val description: String?,
    val artist: String?,
    val copyright: String?
)
