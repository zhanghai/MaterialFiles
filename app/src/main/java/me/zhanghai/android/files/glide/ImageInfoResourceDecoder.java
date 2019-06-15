/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.graphics.BitmapFactory;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ImageInfoResourceDecoder implements ResourceDecoder<InputStream, ImageInfo> {

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) {
        return true;
    }

    @Nullable
    @Override
    public Resource<ImageInfo> decode(@NonNull InputStream source, int width, int height,
                                      @NonNull Options options) throws IOException {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(source, null, bitmapOptions);
        if (bitmapOptions.outWidth == 0 && bitmapOptions.outHeight == 0
                && bitmapOptions.outMimeType == null) {
            throw new IOException("BitmapFactory.decodeFile() failed");
        }
        return new SimpleResource<>(new ImageInfo(bitmapOptions.outWidth, bitmapOptions.outHeight,
                bitmapOptions.outMimeType));
    }
}
