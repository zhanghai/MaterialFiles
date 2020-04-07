/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.FileVisitResult
import java8.nio.file.FileVisitor
import java8.nio.file.Files
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java.io.IOException
import java.io.InterruptedIOException

object WalkFileTreeSearchable {
    @Throws(IOException::class)
    fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        val paths = mutableListOf<Path>()
        // We cannot use Files.find() or Files.walk() because it cannot ignore exceptions.
        Files.walkFileTree(directory, object : FileVisitor<Path> {
            private var lastProgressMillis = System.currentTimeMillis()

            @Throws(InterruptedIOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                visit(directory)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(InterruptedIOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                visit(file)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(InterruptedIOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                if (exception is InterruptedIOException) {
                    throw exception
                }
                exception.printStackTrace()
                visit(file)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(InterruptedIOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                if (exception is InterruptedIOException) {
                    throw (exception as InterruptedIOException?)!!
                }
                exception?.printStackTrace()
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            private fun visit(path: Path) {
                // Exclude the directory being searched.
                if (path == directory) {
                    return
                }
                val fileName = path.fileName
                if (fileName == null || !fileName.toString().contains(query, true)) {
                    return
                }
                paths.add(path)
                val currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis >= lastProgressMillis + intervalMillis) {
                    listener(paths)
                    lastProgressMillis = currentTimeMillis
                    paths.clear()
                }
            }
        })
        if (paths.isNotEmpty()) {
            listener(paths)
        }
    }

    @Throws(InterruptedIOException::class)
    private fun throwIfInterrupted() {
        if (Thread.interrupted()) {
            throw InterruptedIOException()
        }
    }
}
