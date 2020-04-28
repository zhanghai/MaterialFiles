/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.getPath
import me.zhanghai.android.files.provider.common.toByteString

object PathFileNameUtils {
    private const val EXTENSION_SEPARATOR = '.'.toByte()

    // https://github.com/linuxmint/nemo/blob/dfc39a5f13e0af38c088c091e7c2057b1c80e402/eel/eel-vfs-extensions.c#L130
    private val TWO_SEPARATORS_EXTENSIONS = setOf(
        "bz",
        "bz2",
        "gz",
        "sit",
        "Z",
        "xz"
    ).mapTo(mutableSetOf()) { it.toByteString() }

    fun getFullBaseName(fileName: ByteString): ByteString {
        val index = indexOfFullExtensionSeparator(fileName)
        return if (index != -1) fileName.substring(0, index) else fileName
    }

    fun getFullBaseName(fileName: Path): Path {
        return fileName.fileSystem.getPath(getFullBaseName(fileName.toByteString()))
    }

    fun indexOfFullExtensionSeparator(fileName: ByteString): Int {
        val lastExtensionSeparatorIndex = fileName.lastIndexOf(
            EXTENSION_SEPARATOR
        )
        if (lastExtensionSeparatorIndex == -1 || lastExtensionSeparatorIndex == 0) {
            return lastExtensionSeparatorIndex
        }
        val extension = fileName.substring(
            lastExtensionSeparatorIndex + 1
        )
        if (extension !in TWO_SEPARATORS_EXTENSIONS) {
            return lastExtensionSeparatorIndex
        }
        val secondLastExtensionSeparatorIndex = fileName.lastIndexOf(
            EXTENSION_SEPARATOR,
            lastExtensionSeparatorIndex - 1
        )
        return if (secondLastExtensionSeparatorIndex == -1) {
            lastExtensionSeparatorIndex
        } else secondLastExtensionSeparatorIndex
    }

    fun indexOfFullExtensionSeparator(fileName: Path): Int {
        return indexOfFullExtensionSeparator(fileName.toByteString())
    }
}
