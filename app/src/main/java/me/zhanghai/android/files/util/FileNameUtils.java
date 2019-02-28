/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * This class assumes the only separator to be '/'.
 *
 * Terminology:
 * <ul>
 * <li>file = path + SEPARATOR + fileName</li>
 * <li>fileName = baseName + EXTENSION_SEPARATOR + extension</li>
 * </ul>
 */
public class FileNameUtils {

    private static final char EXTENSION_SEPARATOR = '.';
    // Not using File.separatorChar so that behavior is consistent and always ready for URIs.
    // Anyway we are on Android. If one day we were moved to Windows, fail-fast is also good.
    private static final char SEPARATOR = '/';

    private FileNameUtils() {}

    @NonNull
    public static String getBaseName(@NonNull String path) {
        return removeExtension(getFileName(path));
    }

    @NonNull
    public static String getExtension(@NonNull String path) {
        int index = indexOfExtensionSeparator(path);
        return index != -1 ? path.substring(index + 1) : "";
    }

    @NonNull
    public static String getFileName(@NonNull String path) {
        int index = indexOfLastSeparator(path);
        return path.substring(index + 1);
    }

    @NonNull
    public static String getDirectory(@NonNull String path) {
        int index = indexOfLastSeparator(path);
        return index != -1 ? path.substring(0, index) : ".";
    }

    @NonNull
    public static String getDirectoryWithEndSeparator(@NonNull String path) {
        // We assume the only separator is '/'.
        return getDirectory(path) + SEPARATOR;
    }

    public static int indexOfExtensionSeparator(@NonNull String path) {
        int lastSeparatorIndex = indexOfLastSeparator(path);
        int lastExtensionSeparatorIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        return lastSeparatorIndex > lastExtensionSeparatorIndex ? -1 : lastExtensionSeparatorIndex;
    }

    public static int indexOfLastSeparator(@NonNull String path) {
        return path.lastIndexOf(SEPARATOR);
    }

    @NonNull
    public static String removeExtension(@NonNull String path) {
        int index = indexOfExtensionSeparator(path);
        return index != -1 ? path.substring(0, index) : path;
    }

    @NonNull
    public static String replaceExtension(@NonNull String path, @NonNull String extension) {
        path = removeExtension(path);
        if (!TextUtils.isEmpty(extension)) {
            path += EXTENSION_SEPARATOR + extension;
        }
        return path;
    }

    public static boolean isValidFileName(@NonNull String fileName) {
        return !TextUtils.isEmpty(fileName) && fileName.indexOf('/') == -1
                && fileName.indexOf('\0') == -1;
    }

    public static boolean isValidPath(@NonNull String path) {
        return !TextUtils.isEmpty(path) && path.indexOf('\0') == -1;
    }
}
