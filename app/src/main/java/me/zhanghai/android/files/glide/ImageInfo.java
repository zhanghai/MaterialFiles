/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import androidx.annotation.NonNull;

public class ImageInfo {

    public final int width;
    public final int height;
    @NonNull
    public final String mimeType;

    public ImageInfo(int width, int height, @NonNull String mimeType) {
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
    }
}
