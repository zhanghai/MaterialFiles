/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.graphics.drawable.PictureDrawable
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target

// https://github.com/bumptech/glide/blob/master/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgSoftwareLayerSetter.java
class SvgRequestListener : RequestListener<Any> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Any>,
        isFirstResource: Boolean
    ): Boolean {
        if (target is DrawableImageViewTarget) {
            target.view.setLayerType(View.LAYER_TYPE_NONE, null)
        }
        return false
    }

    override fun onResourceReady(
        resource: Any,
        model: Any,
        target: Target<Any>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (target is DrawableImageViewTarget) {
            val layerType = if (resource is PictureDrawable) {
                View.LAYER_TYPE_SOFTWARE
            } else {
                View.LAYER_TYPE_NONE
            }
            target.view.setLayerType(layerType, null)
        }
        return false
    }
}
