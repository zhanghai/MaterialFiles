/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.DirectoryIteratorException
import java8.nio.file.DirectoryStream
import java8.nio.file.Path
import java.io.Closeable
import java.io.IOException

open class PathIteratorDirectoryStream(
    private val iterator: Iterator<Path>,
    private val closeable: Closeable?,
    private val filter: DirectoryStream.Filter<in Path>
) : DirectoryStream<Path> {
    private var isClosed = false

    private var isIteratorReturned = false

    private val lock = Any()

    // TODO: Should return Iterator<Path>
    override fun iterator(): MutableIterator<Path> {
        synchronized(lock) {
            check(!isClosed) { "This directory stream is closed" }
            check(!isIteratorReturned) { "The iterator has already been returned" }
            val filteredIterator = iterator.asSequence().filter {
                !isClosed && try {
                    filter.accept(it)
                } catch (e: IOException) {
                    throw DirectoryIteratorException(e)
                }
            }.iterator()
            return object : MutableIterator<Path> {
                override fun hasNext(): Boolean = synchronized(lock) { filteredIterator.hasNext() }

                override fun next(): Path = synchronized(lock) { filteredIterator.next() }

                override fun remove() {
                    throw UnsupportedOperationException()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        synchronized(lock) {
            if (isClosed) {
                return
            }
            closeable?.close()
            isClosed = true
        }
    }
}
