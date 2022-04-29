/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.AtomicMoveNotSupportedException
import java8.nio.file.CopyOption
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.LinkOption
import java8.nio.file.NoSuchFileException
import java8.nio.file.Path
import java8.nio.file.StandardCopyOption
import java8.nio.file.StandardOpenOption
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.BasicFileAttributes
import java.io.IOException

internal object ForeignCopyMove {
    @Throws(IOException::class)
    fun copy(source: Path, target: Path, vararg options: CopyOption) {
        val copyOptions = options.toCopyOptions()
        if (copyOptions.atomicMove) {
            throw UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString())
        }
        val linkOptions = if (copyOptions.noFollowLinks) {
            arrayOf(LinkOption.NOFOLLOW_LINKS)
        } else {
            emptyArray()
        }
        val sourceAttributes = source.readAttributes(BasicFileAttributes::class.java, *linkOptions)
        if (!(sourceAttributes.isRegularFile || sourceAttributes.isDirectory
                || sourceAttributes.isSymbolicLink)) {
            throw IOException("Cannot copy special file to foreign provider")
        }
        if (!copyOptions.replaceExisting && target.exists(LinkOption.NOFOLLOW_LINKS)) {
            throw FileAlreadyExistsException(source.toString(), target.toString(), null)
        }
        when {
            sourceAttributes.isRegularFile -> {
                if (copyOptions.replaceExisting) {
                    target.deleteIfExists()
                }
                val openOptions = if (copyOptions.noFollowLinks) {
                    arrayOf(LinkOption.NOFOLLOW_LINKS)
                } else {
                    emptyArray()
                }
                source.newInputStream(*openOptions).use { inputStream ->
                    val outputStream = target.newOutputStream(
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
                    )
                    var successful = false
                    try {
                        inputStream.copyTo(
                            outputStream, copyOptions.progressIntervalMillis,
                            copyOptions.progressListener
                        )
                        successful = true
                    } finally {
                        try {
                            outputStream.close()
                        } finally {
                            if (!successful) {
                                try {
                                    target.deleteIfExists()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                } catch (e: UnsupportedOperationException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
            sourceAttributes.isDirectory -> {
                if (copyOptions.replaceExisting) {
                    target.deleteIfExists()
                }
                target.createDirectory()
                copyOptions.progressListener?.invoke(sourceAttributes.size())
            }
            sourceAttributes.isSymbolicLink -> {
                val sourceTarget = source.readSymbolicLink()
                try {
                    // Might throw UnsupportedOperationException, so we cannot delete beforehand.
                    target.createSymbolicLink(sourceTarget)
                } catch (e: FileAlreadyExistsException) {
                    if (!copyOptions.replaceExisting) {
                        throw e
                    }
                    target.deleteIfExists()
                    target.createSymbolicLink(sourceTarget)
                }
                copyOptions.progressListener?.invoke(sourceAttributes.size())
            }
            else -> throw AssertionError()
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from
        // now on.
        val targetAttributeView = target.getFileAttributeView(BasicFileAttributeView::class.java)!!
        val lastModifiedTime = sourceAttributes.lastModifiedTime()
        val lastAccessTime = if (copyOptions.copyAttributes) {
            sourceAttributes.lastAccessTime()
        } else {
            null
        }
        val creationTime = if (copyOptions.copyAttributes) sourceAttributes.creationTime() else null
        try {
            targetAttributeView.setTimes(lastModifiedTime, lastAccessTime, creationTime)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun move(source: Path, target: Path, vararg options: CopyOption) {
        val copyOptions = options.toCopyOptions()
        if (copyOptions.atomicMove) {
            throw AtomicMoveNotSupportedException(
                source.toString(), target.toString(),
                "Cannot move file atomically to foreign provider"
            )
        }
        val optionsForCopy = if (copyOptions.copyAttributes && copyOptions.noFollowLinks) {
            options
        } else {
            CopyOptions(
                copyOptions.replaceExisting, true, false, true, copyOptions.progressIntervalMillis,
                copyOptions.progressListener
            ).toArray()
        }
        copy(source, target, *optionsForCopy)
        try {
            source.delete()
        } catch (e: IOException) {
            if (e !is NoSuchFileException) {
                try {
                    target.delete()
                } catch (e2: IOException) {
                    e.addSuppressed(e2)
                } catch (e2: UnsupportedOperationException) {
                    e.addSuppressed(e2)
                }
            }
            throw e
        } catch (e: UnsupportedOperationException) {
            try {
                target.delete()
            } catch (e2: IOException) {
                e.addSuppressed(e2)
            } catch (e2: UnsupportedOperationException) {
                e.addSuppressed(e2)
            }
            throw e
        }
    }
}
