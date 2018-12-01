/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.util.Base64;

import androidx.annotation.NonNull;

public class IoUtils {

    private IoUtils() {}

    @NonNull
    public static String byteArrayToBase64(@NonNull byte[] bytes) {
        // We are using Base64 in Json so we don't want newlines here.
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    @NonNull
    public static byte[] base64ToByteArray(@NonNull String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }
}
