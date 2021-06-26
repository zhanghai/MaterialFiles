/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.dropLast
import me.zhanghai.android.files.provider.common.dropLastWhile
import me.zhanghai.android.files.provider.common.substringAfterLast
import me.zhanghai.android.files.provider.common.takeIfNotEmpty

@JvmInline
value class BytePathName(val value: ByteString) {
    val fileName: ByteString?
        get() = value.substringAfterLast(SEPARATOR).takeIfNotEmpty()

    val directoryName: ByteString?
        get() = value.substringBeforeLastOrNull(SEPARATOR)?.dropLastWhile { it == SEPARATOR }

    private fun ByteString.substringBeforeLastOrNull(delimiter: Byte): ByteString? {
        val index = lastIndexOf(delimiter)
        return if (index != -1) substring(0, index) else null
    }

    companion object {
        // Not using File.separatorChar so that behavior is consistent and always ready for URIs.
        // Anyway we are on Android. If one day we were moved to Windows, fail-fast is also good.
        const val SEPARATOR = '/'.code.toByte()
    }
}

fun ByteString.asPathName(): BytePathName {
    require(isValidPathName)
    return BytePathName(this)
}

fun ByteString.asPathNameOrNull(): BytePathName? = if (isValidPathName) BytePathName(this) else null

private val ByteString.isValidPathName: Boolean
    get() = isNotEmpty() && !contains('\u0000'.code.toByte())

@JvmInline
value class ByteFileName(val value: ByteString) {
    val singleExtension: ByteString
        get() = value.substringAfterLast(EXTENSION_SEPARATOR, ByteString.EMPTY)

    val extensions: ByteString
        get() {
            val lastExtension = singleExtension
            val lastExtensionString = lastExtension.toString()
            if (DOUBLE_EXTENSIONS.any { lastExtensionString.equals(it, true) }) {
                val secondLastExtension = value.dropLast(lastExtension.length + 1)
                    .substringAfterLast(EXTENSION_SEPARATOR, ByteString.EMPTY)
                if (secondLastExtension.isNotEmpty()) {
                    return ByteStringBuilder()
                        .append(secondLastExtension)
                        .append(EXTENSION_SEPARATOR)
                        .append(lastExtension)
                        .toByteString()
                }
            }
            return lastExtension
        }

    val baseName: ByteString
        get() {
            val extensions = extensions
            return if (extensions.isNotEmpty()) value.dropLast(extensions.length + 1) else value
        }

    companion object {
        const val EXTENSION_SEPARATOR = '.'.code.toByte()

        // https://github.com/GNOME/nautilus/blob/c73ad94a72f8e9a989b01858018de74182d17f0e/eel/eel-vfs-extensions.c#L124
        private val DOUBLE_EXTENSIONS = listOf("bz", "bz2", "gz", "sit", "xz", "Z")
    }
}

fun ByteString.asFileName(): ByteFileName {
    require(isValidFileName)
    return ByteFileName(this)
}

fun ByteString.asFileNameOrNull(): ByteFileName? = if (isValidFileName) ByteFileName(this) else null

private val ByteString.isValidFileName: Boolean
    get() = isNotEmpty() && !contains('\u0000'.code.toByte()) && !contains(BytePathName.SEPARATOR)
