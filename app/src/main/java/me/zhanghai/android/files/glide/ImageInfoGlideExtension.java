/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;

@GlideExtension
public class ImageInfoGlideExtension {

    private static final RequestOptions REQUEST_OPTIONS = RequestOptions
            .decodeTypeOf(ImageInfo.class)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .lock();

    private ImageInfoGlideExtension() {}

    @GlideType(ImageInfo.class)
    @NonNull
    public static RequestBuilder<ImageInfo> asImageInfo(
            @NonNull RequestBuilder<ImageInfo> requestBuilder) {
        return requestBuilder.apply(REQUEST_OPTIONS);
    }
}
