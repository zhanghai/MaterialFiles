/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

// https://github.com/bumptech/glide/blob/master/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgDecoder.java
// https://github.com/bumptech/glide/blob/master/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgDrawableTranscoder.java
class SvgResourceDecoder : ResourceDecoder<InputStream, Drawable> {
    // TODO: Can we tell?
    override fun handles(source: InputStream, options: Options): Boolean = true

    @Throws(IOException::class)
    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<Drawable>? {
        val svg = try {
            SVG.getFromInputStream(source)
        } catch (ex: SVGParseException) {
            throw IOException("SVG.getFromInputStream() failed", ex)
        }
        return SimpleResource(PictureDrawable(svg.renderToPicture()))
    }
}
