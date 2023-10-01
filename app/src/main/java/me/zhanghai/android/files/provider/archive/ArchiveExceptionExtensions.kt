/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.system.OsConstants
import java8.nio.file.FileSystemException
import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.DelegateInputStream
import me.zhanghai.android.libarchive.ArchiveException
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException

// See also libarchive/archive_platform.h .
private const val ARCHIVE_ERRNO_MISC = -1

fun ArchiveException.toFileSystemOrInterruptedIOException(file: Path): IOException =
    when {
        // See also ReadArchive.toArchiveException .
        code == OsConstants.EINTR -> InterruptedIOException(message)
        // See also libarchive/archive_read_support_format_zip.c .
        code == ARCHIVE_ERRNO_MISC && (
            message == "Incorrect passphrase" || message == "Passphrase required for this entry"
        ) -> ArchivePasswordRequiredException(file, message)
        else -> FileSystemException(file.toString(), null, message)
    }.apply { initCause(this@toFileSystemOrInterruptedIOException) }

class ArchiveExceptionInputStream(
    inputStream: InputStream,
    private val file: Path
) : DelegateInputStream(inputStream) {
    @Throws(IOException::class)
    override fun read(): Int =
        try {
            super.read()
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int =
        try {
            super.read(b)
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int =
        try {
            super.read(b, off, len)
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }

    @Throws(IOException::class)
    override fun skip(n: Long): Long = try {
        super.skip(n)
    } catch (e: ArchiveException) {
        throw e.toFileSystemOrInterruptedIOException(file)
    }

    @Throws(IOException::class)
    override fun available(): Int =
        try {
            super.available()
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }

    @Throws(IOException::class)
    override fun close() {
        try {
            super.close()
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }
    }

    @Throws(IOException::class)
    override fun reset() {
        try {
            super.reset()
        } catch (e: ArchiveException) {
            throw e.toFileSystemOrInterruptedIOException(file)
        }
    }
}
