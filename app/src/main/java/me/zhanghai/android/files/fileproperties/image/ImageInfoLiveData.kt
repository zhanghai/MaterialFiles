/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.exifinterface.media.ExifInterface
import com.caverock.androidsvg.SVG
import java8.nio.file.Path
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asMimeTypeOrNull
import me.zhanghai.android.files.fileproperties.PathObserverLiveData
import me.zhanghai.android.files.provider.common.getLastModifiedTime
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import kotlin.math.roundToInt

class ImageInfoLiveData(
    path: Path,
    private val mimeType: MimeType
) : PathObserverLiveData<Stateful<ImageInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading()
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val imageInfo = when (mimeType) {
                    MimeType.IMAGE_SVG_XML -> {
                        val svg = path.newInputStream()
                            .buffered()
                            .use { SVG.getFromInputStream(it) }
                        val width = svg.documentWidth.takeIf { it != -1f }?.roundToInt()
                        val height = svg.documentHeight.takeIf { it != -1f }?.roundToInt()
                        ImageInfo(mimeType, width, height, null)
                    }
                    else -> {
                        val bitmapOptions = BitmapFactory.Options()
                            .apply { inJustDecodeBounds = true }
                        path.newInputStream()
                            .buffered()
                            .use { BitmapFactory.decodeStream(it, null, bitmapOptions) }
                        val mimeType = bitmapOptions.outMimeType?.asMimeTypeOrNull() ?: mimeType
                        val width = bitmapOptions.outWidth.takeIf { it != -1 }
                        val height = bitmapOptions.outHeight.takeIf { it != -1 }
                        val exifInfo = try {
                            val lastModifiedTime = path.getLastModifiedTime().toInstant()
                            path.newInputStream().buffered().use {
                                val exifInterface = ExifInterface(it)
                                val dateTimeOriginal =
                                    exifInterface.inferDateTimeOriginal(lastModifiedTime)
                                val gpsLatitudeLongitude = exifInterface.latLong
                                    ?.let { Pair(it[0], it[1]) }
                                val gpsAltitude = exifInterface.gpsAltitude
                                val make =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_MAKE)
                                val model =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_MODEL)
                                val fNumber = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_F_NUMBER
                                )
                                val shutterSpeedValue = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_SHUTTER_SPEED_VALUE
                                )
                                val focalLength = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_FOCAL_LENGTH
                                )
                                val photographicSensitivity = exifInterface.getAttributeIntOrNull(
                                    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY
                                )
                                val software =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_SOFTWARE)
                                val description = exifInterface.getAttributeNotBlank(
                                    ExifInterface.TAG_IMAGE_DESCRIPTION
                                ) ?: exifInterface.getAttributeNotBlank(
                                    ExifInterface.TAG_USER_COMMENT
                                )
                                val artist =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_ARTIST)
                                val copyright =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_COPYRIGHT)
                                ExifInfo(
                                    dateTimeOriginal, gpsLatitudeLongitude, gpsAltitude, make,
                                    model, fNumber, shutterSpeedValue, focalLength,
                                    photographicSensitivity, software, description, artist,
                                    copyright
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                        ImageInfo(mimeType, width, height, exifInfo)
                    }
                }
                Success(imageInfo)
            } catch (e: Exception) {
                Failure(e)
            }
            postValue(value)
        }
    }
}
