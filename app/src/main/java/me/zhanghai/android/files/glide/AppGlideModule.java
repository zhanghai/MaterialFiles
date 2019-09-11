/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;

import java.io.InputStream;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import java8.nio.file.Path;

@GlideModule
public class AppGlideModule extends com.bumptech.glide.module.AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            builder.addGlobalRequestListener(new SvgRequestListener());
        }
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(InputStream.class, ImageInfo.class, new ImageInfoResourceDecoder());
        registry.prepend(ApplicationInfo.class, Drawable.class,
                new ApplicationIconModelLoader.Factory(context));
        registry.prepend(Path.class, ByteBuffer.class,
                new MediaEmbeddedPictureModelLoader.Factory());
        registry.prepend(Path.class, Drawable.class, new ApkIconModelLoader.Factory(context));
        registry.prepend(Path.class, Bitmap.class, new DocumentThumbnailModelLoader.Factory());
        registry.append(Path.class, InputStream.class, new PathInputStreamModelLoader.Factory());
        registry.append(Path.class, ParcelFileDescriptor.class,
                new PathParcelFileDescriptorModelLoader.Factory());
        registry.append(InputStream.class, Drawable.class, new SvgResourceDecoder());
    }
}
