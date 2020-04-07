/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideType
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

@GlideExtension
object ImageInfoGlideExtension {
    private val REQUEST_OPTIONS = RequestOptions.decodeTypeOf(ImageInfo::class.java)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .lock()

    @GlideType(ImageInfo::class)
    @JvmStatic
    fun asImageInfo(requestBuilder: RequestBuilder<ImageInfo>): RequestBuilder<ImageInfo> =
        requestBuilder.apply(REQUEST_OPTIONS)
}
