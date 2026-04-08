/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import java8.nio.file.Path
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException

interface RootablePath {
    fun isRootRequired(isAttributeAccess: Boolean): Boolean
}

private val rootStrategy: RootStrategy
    get() = if (isRunningAsRoot) RootStrategy.NEVER else Settings.ROOT_STRATEGY.valueCompat

@Throws(IOException::class)
fun <T, R> callRootable(
    path: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T, block: T.() -> R
): R {
    path as? RootablePath ?: throw IllegalArgumentException("$path is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER -> localObject.block()
        RootStrategy.AUTOMATIC -> {
            // ALWAYS try local first, only use root if local fails with permission error
            try {
                localObject.block()
            } catch (e: IOException) {
                // Only retry with root for permission-related errors
                if (isPermissionError(e)) {
                    try {
                        rootObject.block()
                    } catch (rootE: IOException) {
                        // If root also fails, throw the original local error
                        throw e
                    }
                } else {
                    throw e
                }
            }
        }
        RootStrategy.ALWAYS -> rootObject.block()
    }
}

@Throws(IOException::class)
fun <T, R> callRootable(
    path1: Path,
    path2: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T,
    block: T.() -> R
): R {
    path1 as? RootablePath ?: throw IllegalArgumentException("$path1 is not a RootablePath")
    path2 as? RootablePath ?: throw IllegalArgumentException("$path2 is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER -> localObject.block()
        RootStrategy.AUTOMATIC -> {
            try {
                localObject.block()
            } catch (e: IOException) {
                if (isPermissionError(e)) {
                    try {
                        rootObject.block()
                    } catch (rootE: IOException) {
                        throw e
                    }
                } else {
                    throw e
                }
            }
        }
        RootStrategy.ALWAYS -> rootObject.block()
    }
}

/**
 * Minimal permission error detection
 */
private fun isPermissionError(e: IOException): Boolean {
    val message = e.message ?: ""
    return e is java8.nio.file.AccessDeniedException ||
            message.contains("permission", ignoreCase = true) ||
            message.contains("denied", ignoreCase = true)
}