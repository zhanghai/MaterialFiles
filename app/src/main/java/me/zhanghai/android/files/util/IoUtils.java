/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;

public class IoUtils {

    private static final int BUFFER_SIZE = 4 * 1024;

    private IoUtils() {}

    @NonNull
    public static String inputStreamToString(@NonNull InputStream inputStream,
                                             @NonNull Charset charset) throws IOException {
        return readerToString(new InputStreamReader(inputStream, charset));
    }

    @NonNull
    public static String readerToString(@NonNull Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int length;
        while ((length = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }

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
