/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.image

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import java8.nio.file.Path
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.formatLong
import me.zhanghai.android.files.file.isImage
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.awaitGetFromLocation
import me.zhanghai.android.files.util.createViewLocation
import me.zhanghai.android.files.util.isGeocoderPresent
import me.zhanghai.android.files.util.startActivitySafe
import me.zhanghai.android.files.util.userFriendlyString
import me.zhanghai.android.files.util.viewModels
import kotlin.math.pow
import kotlin.math.roundToInt

class FilePropertiesImageTabFragment : FilePropertiesTabFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels {
        { FilePropertiesImageTabViewModel(args.path, args.mimeType) }
    }

    private var addressJob: Job? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.imageInfoLiveData.observe(viewLifecycleOwner) { onImageInfoChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onImageInfoChanged(stateful: Stateful<ImageInfo>) {
        addressJob?.cancel()
        addressJob = null
        bindView(stateful) { imageInfo ->
            addItemView(
                R.string.file_properties_media_dimensions, if (imageInfo.dimensions != null) {
                    getString(
                        R.string.file_properties_media_dimensions_format,
                        imageInfo.dimensions.width, imageInfo.dimensions.height
                    )
                } else {
                    getString(R.string.unknown)
                }
            )
            val exifInfo = imageInfo.exifInfo
            if (exifInfo != null) {
                if (exifInfo.dateTimeOriginal != null) {
                    addItemView(
                        R.string.file_properties_media_date_time,
                        exifInfo.dateTimeOriginal.formatLong()
                    )
                }
                if (exifInfo.gpsCoordinates != null) {
                    addItemView(
                        R.string.file_properties_media_coordinates, getString(
                            R.string.file_properties_media_coordinates_format,
                            exifInfo.gpsCoordinates.first, exifInfo.gpsCoordinates.second
                        )
                    ) {
                        startActivitySafe(
                            Intent::class.createViewLocation(
                                exifInfo.gpsCoordinates.first.toFloat(),
                                exifInfo.gpsCoordinates.second.toFloat(), args.path.name
                            )
                        )
                    }
                    if (isGeocoderPresent) {
                        val textView = addItemView(
                            R.string.file_properties_media_address, getString(R.string.loading)
                        )
                        val geocoder = Geocoder(requireContext())
                        addressJob = viewLifecycleOwner.lifecycleScope.launch {
                            val address = try {
                                geocoder.awaitGetFromLocation(
                                    exifInfo.gpsCoordinates.first, exifInfo.gpsCoordinates.second, 1
                                ).first()
                            } catch (e: Exception) {
                                null
                            }
                            if (isActive) {
                                textView.text = address?.userFriendlyString
                                    ?: getString(R.string.unknown)
                            }
                        }
                    }
                }
                if (exifInfo.gpsAltitude != null) {
                    addItemView(
                        R.string.file_properties_image_gps_altitude, getString(
                            R.string.file_properties_image_gps_altitude_format, exifInfo.gpsAltitude
                        )
                    )
                }
                val equipment = getEquipment(exifInfo.make, exifInfo.model)
                if (equipment != null) {
                    addItemView(R.string.file_properties_image_equipment, equipment)
                }
                if (exifInfo.fNumber != null) {
                    addItemView(
                        R.string.file_properties_image_f_number, getString(
                            R.string.file_properties_image_f_number_format, exifInfo.fNumber
                        )
                    )
                }
                if (exifInfo.shutterSpeedValue != null) {
                    addItemView(
                        R.string.file_properties_image_shutter_speed,
                        getShutterSpeedText(exifInfo.shutterSpeedValue)
                    )
                }
                if (exifInfo.focalLength != null) {
                    addItemView(
                        R.string.file_properties_image_focal_length, getString(
                            R.string.file_properties_image_focal_length_format, exifInfo.focalLength
                        )
                    )
                }
                if (exifInfo.photographicSensitivity != null) {
                    addItemView(
                        R.string.file_properties_image_photographic_sensitivity, getString(
                            R.string.file_properties_image_photographic_sensitivity_format,
                            exifInfo.photographicSensitivity
                        )
                    )
                }
                if (exifInfo.software != null) {
                    addItemView(R.string.file_properties_image_software, exifInfo.software)
                }
                if (exifInfo.description != null) {
                    addItemView(R.string.file_properties_image_description, exifInfo.description)
                }
                if (exifInfo.artist != null) {
                    addItemView(R.string.file_properties_image_artist, exifInfo.artist)
                }
                if (exifInfo.copyright != null) {
                    addItemView(R.string.file_properties_image_copyright, exifInfo.copyright)
                }
            }
        }
    }

    private fun getEquipment(make: String?, model: String?): String? =
        when {
            make != null && model != null -> {
                if (model.startsWith(make, true)) {
                    model
                } else {
                    getString(R.string.file_properties_image_equipment_format, make, model)
                }
            }
            make != null -> make
            model != null -> model
            else -> null
        }

    // @see com.android.documentsui.inspector.MediaView.formatShutterSpeed
    private fun getShutterSpeedText(value: Double): String =
        if (value <= 0) {
            val shutterSpeed = 2.0.pow(-1 * value)
            ((shutterSpeed * 10.0).roundToInt() / 10.0).toString()
        } else {
            val approximateDenominator = 2.0.pow(value).toInt() + 1
            getString(
                R.string.file_properties_image_shutter_speed_with_denominator_format,
                approximateDenominator
            )
        }

    companion object {
        fun isAvailable(file: FileItem): Boolean = file.mimeType.isImage
    }

    @Parcelize
    class Args(
        val path: @WriteWith<ParcelableParceler> Path,
        val mimeType: MimeType
    ) : ParcelableArgs
}
