/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.glide;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.DrawableImageViewTarget;

// Setting the placeholder drawable as error drawable again causes animation glitches, so we just
// ignore the onLoadFailed callback.
public class IgnoreErrorDrawableImageViewTarget extends DrawableImageViewTarget {

    public IgnoreErrorDrawableImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        // Do nothing.
    }
}
