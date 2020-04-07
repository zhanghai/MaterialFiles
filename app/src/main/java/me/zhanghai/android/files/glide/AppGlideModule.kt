/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.ParcelFileDescriptor
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java8.nio.file.Path
import java.io.InputStream
import java.nio.ByteBuffer

@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            builder.addGlobalRequestListener(SvgRequestListener())
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(InputStream::class.java, ImageInfo::class.java, ImageInfoResourceDecoder())
        registry.prepend(
            ApplicationInfo::class.java, Drawable::class.java, ApplicationIconModelLoader.Factory()
        )
        registry.prepend(
            Path::class.java, ByteBuffer::class.java, MediaEmbeddedPictureModelLoader.Factory()
        )
        registry.prepend(Path::class.java, Drawable::class.java, ApkIconModelLoader.Factory())
        registry.prepend(
            Path::class.java, Bitmap::class.java, DocumentThumbnailModelLoader.Factory()
        )
        registry.append(
            Path::class.java, InputStream::class.java, PathInputStreamModelLoader.Factory()
        )
        registry.append(
            Path::class.java, ParcelFileDescriptor::class.java,
            PathParcelFileDescriptorModelLoader.Factory()
        )
        registry.append(InputStream::class.java, Drawable::class.java, SvgResourceDecoder())
    }
}
