/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy

object DownsampleStrategies {
    val AT_MOST_CENTER_OUTSIDE: DownsampleStrategy = AtMostCenterOutside()

    private class AtMostCenterOutside : DownsampleStrategy() {
        override fun getScaleFactor(
            sourceWidth: Int,
            sourceHeight: Int,
            requestedWidth: Int,
            requestedHeight: Int
        ): Float =
            if (sourceWidth > requestedWidth && sourceHeight > requestedHeight) {
                CENTER_OUTSIDE.getScaleFactor(
                    sourceWidth,
                    sourceHeight,
                    requestedWidth,
                    requestedHeight
                )
            } else {
                NONE.getScaleFactor(sourceWidth, sourceHeight, requestedWidth, requestedHeight)
            }

        override fun getSampleSizeRounding(
            sourceWidth: Int,
            sourceHeight: Int,
            requestedWidth: Int,
            requestedHeight: Int
        ): SampleSizeRounding =
            CENTER_OUTSIDE.getSampleSizeRounding(
                sourceWidth, sourceHeight, requestedWidth, requestedHeight
            )
    }
}
