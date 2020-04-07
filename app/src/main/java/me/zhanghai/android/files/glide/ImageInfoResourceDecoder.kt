/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.graphics.BitmapFactory
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import me.zhanghai.android.files.file.asMimeType
import java.io.IOException
import java.io.InputStream

class ImageInfoResourceDecoder : ResourceDecoder<InputStream, ImageInfo> {
    override fun handles(source: InputStream, options: Options): Boolean = true

    @Throws(IOException::class)
    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<ImageInfo>? {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(source, null, bitmapOptions)
        if (bitmapOptions.outWidth == 0 && bitmapOptions.outHeight == 0
            && bitmapOptions.outMimeType == null) {
            throw IOException("BitmapFactory.decodeFile() failed")
        }
        return SimpleResource(
            ImageInfo(
                bitmapOptions.outWidth, bitmapOptions.outHeight,
                bitmapOptions.outMimeType.asMimeType()
            )
        )
    }
}
