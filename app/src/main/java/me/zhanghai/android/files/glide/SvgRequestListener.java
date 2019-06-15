/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.graphics.drawable.PictureDrawable;
import android.view.View;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// https://github.com/bumptech/glide/blob/master/samples/svg/src/main/java/com/bumptech/glide/samples/svg/SvgSoftwareLayerSetter.java
public class SvgRequestListener implements RequestListener<Object> {

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, @NonNull Object model,
                                @NonNull Target<Object> target, boolean isFirstResource) {
        Target<?> target2 = target;
        if (target2 instanceof DrawableImageViewTarget) {
            DrawableImageViewTarget drawableImageViewTarget = (DrawableImageViewTarget) target2;
            drawableImageViewTarget.getView().setLayerType(View.LAYER_TYPE_NONE, null);
        }
        return false;
    }

    @Override
    public boolean onResourceReady(@NonNull Object resource, @NonNull Object model,
                                   @NonNull Target<Object> target, @NonNull DataSource dataSource,
                                   boolean isFirstResource) {
        Target<?> target2 = target;
        if (target2 instanceof DrawableImageViewTarget) {
            DrawableImageViewTarget drawableImageViewTarget = (DrawableImageViewTarget) target2;
            int layerType = resource instanceof PictureDrawable ? View.LAYER_TYPE_SOFTWARE
                    : View.LAYER_TYPE_NONE;
            drawableImageViewTarget.getView().setLayerType(layerType, null);
        }
        return false;
    }
}
