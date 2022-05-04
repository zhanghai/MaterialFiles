/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import java.io.IOException

abstract class AbstractPath<T : AbstractPath<T>> : CovariantPath<T> {
    override fun getFileName(): T? {
        val nameCount = nameCount
        return if (nameCount != 0) getName(nameCount - 1) else null
    }

    override fun getParent(): T? =
        when (val nameCount = nameCount) {
            0 -> null
            1 -> root
            else -> root!!.resolve(subpath(0, nameCount - 1))
        }

    override fun startsWith(other: String): Boolean = startsWith(fileSystem.getPath(other))

    override fun endsWith(other: String): Boolean = endsWith(fileSystem.getPath(other))

    override fun resolve(other: String): T = resolve(fileSystem.getPath(other))

    @Suppress("UNCHECKED_CAST")
    override fun resolveSibling(other: Path): T =
        parent?.resolve(other) ?: other as T

    override fun resolveSibling(other: String): T = resolveSibling(fileSystem.getPath(other))

    override val names: Iterable<T>
        get() = object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                private var index = 0

                override fun hasNext(): Boolean = index < nameCount

                override fun next(): T {
                    if (index >= nameCount) {
                        throw NoSuchElementException()
                    }
                    return getName(index).also { ++index }
                }
            }
        }

    @Throws(IOException::class)
    override fun register(watcher: WatchService, vararg events: WatchEvent.Kind<*>): WatchKey =
        register(watcher, events)
}
