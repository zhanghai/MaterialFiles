/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.text.TextUtils;

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

    public static String getBaseName(String path) {
        return removeExtension(getFileName(path));
    }

    public static String getExtension(String path) {
        int index = indexOfExtensionSeparator(path);
        return index != -1 ? path.substring(index + 1) : "";
    }

    public static String getFileName(String path) {
        int index = indexOfLastSeparator(path);
        return path.substring(index + 1);
    }

    public static String getDirectory(String path) {
        int index = indexOfLastSeparator(path);
        return index != -1 ? path.substring(0, index) : ".";
    }

    public static String getDirectoryWithEndSeparator(String path) {
        // We assume the only separator is '/'.
        return getDirectory(path) + SEPARATOR;
    }

    public static int indexOfExtensionSeparator(String path) {
        int lastSeparatorIndex = indexOfLastSeparator(path);
        int lastExtensionSeparatorIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        return lastSeparatorIndex > lastExtensionSeparatorIndex ? -1 : lastExtensionSeparatorIndex;
    }

    public static int indexOfLastSeparator(String path) {
        return path.lastIndexOf(SEPARATOR);
    }

    public static String removeExtension(String path) {
        int index = indexOfExtensionSeparator(path);
        return index != -1 ? path.substring(0, index) : path;
    }

    public static String replaceExtension(String path, String extension) {
        path = removeExtension(path);
        if (!TextUtils.isEmpty(extension)) {
            path += EXTENSION_SEPARATOR + extension;
        }
        return path;
    }

    public static boolean isValidFileName(String fileName) {
        return !fileName.isEmpty() && fileName.indexOf('/') == -1 && fileName.indexOf('\0') == -1;
    }

    public static boolean isValidPath(String path) {
        return path.indexOf('\0') == -1;
    }
}
