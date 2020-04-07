/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.DrawableImageViewTarget

// Setting the placeholder drawable as error drawable again causes animation glitches, so we just
// ignore the onLoadFailed callback.
class IgnoreErrorDrawableImageViewTarget(view: ImageView) : DrawableImageViewTarget(view) {
    override fun onLoadFailed(errorDrawable: Drawable?) {
        // Do nothing.
    }
}
