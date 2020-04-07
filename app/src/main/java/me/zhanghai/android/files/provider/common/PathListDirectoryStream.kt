/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.DirectoryIteratorException
import java8.nio.file.DirectoryStream
import java8.nio.file.Path
import java.io.IOException
import java.util.NoSuchElementException

class PathListDirectoryStream(
    private val paths: List<Path>,
    private val filter: DirectoryStream.Filter<in Path>
) : DirectoryStream<Path> {
    private var iterator: PathIterator? = null

    private var isClosed = false

    private val lock = Any()

    // TODO: Should return Iterator<Path>
    override fun iterator(): MutableIterator<Path> {
        synchronized(lock) {
            check(!isClosed) { "This directory stream is closed" }
            var iterator = iterator
            check(iterator == null) { "The iterator has already been returned" }
            iterator = PathIterator()
            this.iterator = iterator
            return iterator
        }
    }

    override fun close() {
        synchronized(lock) { isClosed = true }
    }

    // TODO: Try kotlin.sequences.iterator()?
    private inner class PathIterator : MutableIterator<Path> {
        private val iterator = paths.iterator()

        private var nextPath: Path? = null

        private var isEndOfStreamReached = false

        override fun hasNext(): Boolean {
            synchronized(lock) {
                if (nextPath != null) {
                    return true
                }
                if (isEndOfStreamReached) {
                    return false
                }
                nextPath = getNextPathLocked()
                isEndOfStreamReached = nextPath == null
                return !isEndOfStreamReached
            }
        }

        private fun getNextPathLocked(): Path? {
            while (true) {
                if (isClosed) {
                    return null
                }
                if (!iterator.hasNext()) {
                    return null
                }
                val path = iterator.next()
                val accepted = try {
                    filter.accept(path)
                } catch (e: IOException) {
                    throw DirectoryIteratorException(e)
                }
                if (!accepted) {
                    continue
                }
                return path
            }
        }

        override fun next(): Path {
            synchronized(lock) {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                val path = nextPath!!
                nextPath = null
                return path
            }
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }
}
