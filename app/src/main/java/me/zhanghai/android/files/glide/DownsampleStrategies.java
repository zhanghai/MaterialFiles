/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;

import androidx.annotation.NonNull;

public class DownsampleStrategies {

    private DownsampleStrategies() {}

    public static final DownsampleStrategy AT_MOST_CENTER_OUTSIDE = new AtMostCenterOutside();

    private static class AtMostCenterOutside extends DownsampleStrategy {

        @Override
        public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
                                    int requestedHeight) {
            if (sourceWidth > requestedWidth && sourceHeight > requestedHeight) {
                return DownsampleStrategy.CENTER_OUTSIDE.getScaleFactor(sourceWidth, sourceHeight,
                        requestedWidth, requestedHeight);
            } else {
                return DownsampleStrategy.NONE.getScaleFactor(sourceWidth, sourceHeight,
                        requestedWidth, requestedHeight);
            }
        }

        @NonNull
        @Override
        public SampleSizeRounding getSampleSizeRounding(int sourceWidth, int sourceHeight,
                                                        int requestedWidth, int requestedHeight) {
            return DownsampleStrategy.CENTER_OUTSIDE.getSampleSizeRounding(sourceWidth,
                    sourceHeight, requestedWidth, requestedHeight);
        }
    }
}
