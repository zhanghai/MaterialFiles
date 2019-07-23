/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;

import androidx.annotation.NonNull;

public class IoUtils {

    private static final int BUFFER_SIZE = 4 * 1024;

    /*
     * @see java.util.ArrayList#MAX_ARRAY_SIZE
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    private IoUtils() {}

    @NonNull
    public static byte[] inputStreamToByteArray(@NonNull InputStream inputStream,
                                                int initialCapacity) throws IOException {
        byte[] bytes = new byte[initialCapacity];
        int size = 0;
        while (true) {
            int readSize;
            while ((readSize = inputStream.read(bytes, size, bytes.length - size)) > 0) {
                size += readSize;
            }
            if (readSize == -1) {
                break;
            }
            int nextByte = inputStream.read();
            if (nextByte == -1) {
                break;
            }
            if (bytes.length == MAX_BUFFER_SIZE) {
                throw new OutOfMemoryError();
            }
            int newCapacity = bytes.length << 1;
            if (newCapacity < 0 || newCapacity > MAX_BUFFER_SIZE) {
                newCapacity = MAX_BUFFER_SIZE;
            }
            newCapacity = Math.max(newCapacity, BUFFER_SIZE);
            bytes = Arrays.copyOf(bytes, newCapacity);
            bytes[size] = (byte) nextByte;
            ++size;
        }
        return bytes.length == size ? bytes : Arrays.copyOf(bytes, size);
    }

    public static long inputStreamToOutputStream(@NonNull InputStream inputStream,
                                                 @NonNull OutputStream outputStream)
            throws IOException {
        long count = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
            count += length;
        }
        return count;
    }

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

    public static void close(@NonNull AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
