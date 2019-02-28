/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.DrawableImageViewTarget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Setting the placeholder drawable as error drawable again causes animation glitches, so we just
// ignore the onLoadFailed callback.
public class IgnoreErrorDrawableImageViewTarget extends DrawableImageViewTarget {

    public IgnoreErrorDrawableImageViewTarget(@NonNull ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        // Do nothing.
    }
}
