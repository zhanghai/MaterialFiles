/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.video

import android.util.Size
import org.threeten.bp.Duration
import org.threeten.bp.Instant

// @see com.android.providers.media.scan.ModernMediaScanner.scanItemVideo
// @see com.android.documentsui.inspector.MediaView.showVideoData
// @see https://github.com/GNOME/nautilus/blob/c73ad94a72f8e9a989b01858018de74182d17f0e/extensions/audio-video-properties/bacon-video-widget-properties.c#L89
class VideoInfo(
    val title: String?,
    val dimensions: Size?,
    val duration: Duration?,
    val date: Instant?,
    val location: Pair<Float, Float>?,
    val bitRate: Long?
)
