/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

inline class PathName(val value: String) {
    val directory: String
        get() = value
            .dropLastWhile { it == SEPARATOR }
            .substringBeforeLast(SEPARATOR, "")
            .dropLastWhile { it == SEPARATOR }

    val fileName: String
        get() = value.dropLastWhile { it == SEPARATOR }.substringAfterLast(SEPARATOR)

    val isFileName: Boolean
        get() = value == fileName

    val baseName: String
        get() = fileName.substringBeforeLast(EXTENSION_SEPARATOR)

    val extension: String
        get() = fileName.substringAfterLast(EXTENSION_SEPARATOR, "")

    companion object {
        // Not using File.separatorChar so that behavior is consistent and always ready for URIs.
        // Anyway we are on Android. If one day we were moved to Windows, fail-fast is also good.
        const val SEPARATOR = '/'

        const val EXTENSION_SEPARATOR = '.'
    }
}

fun String.asPathName(): PathName {
    require(isValidPathName)
    return PathName(this)
}

fun String.asPathNameOrNull(): PathName? = if (isValidPathName) PathName(this) else null

private val String.isValidPathName: Boolean
    get() = isNotEmpty() && !contains('\u0000')
