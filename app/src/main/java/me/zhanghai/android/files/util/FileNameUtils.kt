/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.text.TextUtils

/**
 * This class assumes the only separator to be '/'.
 *
 * Terminology:
 *
 *  * file = path + SEPARATOR + fileName
 *  * fileName = baseName + EXTENSION_SEPARATOR + extension
 *
 */
object FileNameUtils {
    private const val EXTENSION_SEPARATOR = '.'

    // Not using File.separatorChar so that behavior is consistent and always ready for URIs.
    // Anyway we are on Android. If one day we were moved to Windows, fail-fast is also good.
    private const val SEPARATOR = '/'
    fun getBaseName(path: String): String {
        return removeExtension(
            getFileName(path)
        )
    }

    fun getExtension(path: String): String {
        val index = indexOfExtensionSeparator(
            path
        )
        return if (index != -1) path.substring(index + 1) else ""
    }

    fun getFileName(path: String): String {
        val index = indexOfLastSeparator(path)
        return path.substring(index + 1)
    }

    fun getDirectory(path: String): String {
        val index = indexOfLastSeparator(path)
        return if (index != -1) path.substring(0, index) else "."
    }

    fun getDirectoryWithEndSeparator(path: String): String {
        // We assume the only separator is '/'.
        return getDirectory(
            path
        ) + SEPARATOR
    }

    fun indexOfExtensionSeparator(path: String): Int {
        val lastSeparatorIndex = indexOfLastSeparator(
            path
        )
        val lastExtensionSeparatorIndex = path.lastIndexOf(
            EXTENSION_SEPARATOR
        )
        return if (lastSeparatorIndex > lastExtensionSeparatorIndex) -1 else lastExtensionSeparatorIndex
    }

    fun indexOfLastSeparator(path: String): Int {
        return path.lastIndexOf(SEPARATOR)
    }

    fun removeExtension(path: String): String {
        val index = indexOfExtensionSeparator(
            path
        )
        return if (index != -1) path.substring(0, index) else path
    }

    fun replaceExtension(path: String, extension: String): String {
        var path = path
        path = removeExtension(path)
        if (!TextUtils.isEmpty(extension)) {
            path += EXTENSION_SEPARATOR.toString() + extension
        }
        return path
    }

    fun isValidFileName(fileName: String): Boolean {
        return !TextUtils.isEmpty(fileName) && fileName.indexOf('/') == -1 && fileName.indexOf(
            '\u0000'
        ) == -1
    }

    fun isValidPath(path: String): Boolean {
        return !TextUtils.isEmpty(path) && path.indexOf('\u0000') == -1
    }
}
