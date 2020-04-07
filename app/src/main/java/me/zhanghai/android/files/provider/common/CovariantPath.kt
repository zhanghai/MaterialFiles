/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.LinkOption
import java8.nio.file.Path
import java.io.IOException

interface CovariantPath<T : CovariantPath<T>> : Path {
    override fun getRoot(): T?

    override fun getFileName(): T?

    override fun getParent(): T?

    override fun getName(index: Int): T

    override fun subpath(beginIndex: Int, endIndex: Int): T

    override fun normalize(): T

    override fun resolve(other: Path): T

    override fun resolve(other: String): T

    override fun resolveSibling(other: Path): T

    override fun resolveSibling(other: String): T

    override fun relativize(other: Path): T

    override fun toAbsolutePath(): T

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): T

    override fun iterator(): MutableIterator<Path> {
        throw UnsupportedOperationException()
    }

    val names: Iterable<T>
}
