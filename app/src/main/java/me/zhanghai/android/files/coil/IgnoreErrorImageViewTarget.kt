/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.Coil
import coil.ImageLoader
import coil.request.LoadRequest
import coil.request.LoadRequestBuilder
import coil.request.RequestDisposable
import coil.target.ImageViewTarget

// Setting the placeholder drawable as error drawable again causes animation glitches, so we just
// ignore the onError() callback.
class IgnoreErrorImageViewTarget(view: ImageView) : ImageViewTarget(view) {
    override fun onError(error: Drawable?) {}
}

inline fun ImageView.loadAnyIgnoringError(
    data: Any?,
    imageLoader: ImageLoader = Coil.imageLoader(context),
    builder: LoadRequestBuilder.() -> Unit = {}
): RequestDisposable {
    val request = LoadRequest.Builder(context)
        .data(data)
        .target(IgnoreErrorImageViewTarget(this))
        .apply(builder)
        .build()
    return imageLoader.execute(request)
}
