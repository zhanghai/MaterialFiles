/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import java8.nio.file.AccessDeniedException
import java8.nio.file.Path
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException

interface RootablePath {
    var isRootPreferred: Boolean

    val rootStrategy: RootStrategy
        get() {
            if (isRunningAsRoot) {
                return RootStrategy.NEVER
            }
            val strategy = Settings.ROOT_STRATEGY.valueCompat
            if (strategy == RootStrategy.PREFER_NO && isRootPreferred) {
                return RootStrategy.PREFER_YES
            }
            return strategy
        }
}

@Throws(IOException::class)
fun <T, R> callRootable(path: Path, localObject: T, rootObject: T, block: T.() -> R): R {
    path as? RootablePath ?: throw IllegalArgumentException("$path is not a RootablePath")
    return when (path.rootStrategy) {
        RootStrategy.NEVER -> localObject.block()
        RootStrategy.PREFER_NO ->
            try {
                localObject.block()
            } catch (e: AccessDeniedException) {
                // Ignored.
                rootObject.block().also { path.isRootPreferred = true }
            }
        RootStrategy.PREFER_YES -> {
            try {
                rootObject.block()
            } catch (e: RemoteFileSystemException) {
                e.printStackTrace()
                localObject.block()
            }
        }
        RootStrategy.ALWAYS -> rootObject.block()
    }
}

@Throws(IOException::class)
fun <T, R> callRootable(
    path1: Path,
    path2: Path,
    localObject: T,
    rootObject: T,
    block: T.() -> R
): R {
    path1 as? RootablePath ?: throw IllegalArgumentException("$path1 is not a RootablePath")
    path2 as? RootablePath ?: throw IllegalArgumentException("$path2 is not a RootablePath")
    val strategy1 = path1.rootStrategy
    val strategy2 = path2.rootStrategy
    return when {
        strategy1 == RootStrategy.NEVER || strategy2 == RootStrategy.NEVER ->
            localObject.block()
        strategy1 == RootStrategy.ALWAYS || strategy2 == RootStrategy.ALWAYS ->
            rootObject.block()
        strategy1 == RootStrategy.PREFER_YES || strategy2 == RootStrategy.PREFER_YES ->
            // We let PREFER_YES win over PREFER_NO because user can reject a root request, but
            // not vice versa.
            try {
                rootObject.block()
            } catch (e: RemoteFileSystemException) {
                e.printStackTrace()
                localObject.block()
            }
        else ->
            try {
                localObject.block()
            } catch (e: AccessDeniedException) {
                // Ignored.
                // We don't know which path(s) should prefer using root afterwards, so just skip
                // setting isRootPreferred.
                rootObject.block()
            }
    }
}
