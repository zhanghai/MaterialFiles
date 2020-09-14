/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper
import org.apache.commons.compress.utils.IOUtils
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.math.max

internal class RarFile(file: File, encoding: String?) : Closeable {
    private var archive =
        try {
            Archive(file)
        } catch (e: RarException) {
            throw ArchiveException(e)
        }

    private val zipEncoding = ZipEncodingHelper.getZipEncoding(encoding)

    @get:Throws(IOException::class)
    val nextEntry: RarArchiveEntry?
        get() = archive.nextFileHeader()?.let { RarArchiveEntry(it, zipEncoding) }

    @get:Throws(IOException::class)
    val entries: Iterable<RarArchiveEntry>
        get() {
            val entries = mutableListOf<RarArchiveEntry>()
            for (header in archive.fileHeaders) {
                entries += RarArchiveEntry(header, zipEncoding)
            }
            return entries
        }

    @Throws(IOException::class)
    fun getInputStream(entry: RarArchiveEntry): InputStream {
        val inputStream = PipedInputStream()
        val outputStream = PipedOutputStream(inputStream)
        Thread {
            try {
                outputStream.use { archive.extractFile(entry.header, it) }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RarException) {
                e.printStackTrace()
            }
        }.start()
        return inputStream
    }

    @Throws(IOException::class)
    override fun close() {
        archive.close()
    }

    companion object {
        const val RAR = "rar"

        private val SIGNATURE_OLD = byteArrayOf(0x52, 0x45, 0x7e, 0x5e)
        private val SIGNATURE_V4 = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
        private val SIGNATURE_V5 = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01)

        @Throws(IOException::class)
        fun detect(inputStream: InputStream): String? {
            require(inputStream.markSupported()) { "InputStream.markSupported() returned false" }
            val signature = ByteArray(max(SIGNATURE_OLD.size, SIGNATURE_V4.size))
            inputStream.mark(signature.size)
            val signatureLength = try {
                IOUtils.readFully(inputStream, signature)
            } finally {
                inputStream.reset()
            }
            return if (matches(signature, signatureLength)) RAR else null
        }

        private fun matches(signature: ByteArray, length: Int): Boolean =
            matches(signature, length, SIGNATURE_OLD)
                || matches(signature, length, SIGNATURE_V4)
                || matches(signature, length, SIGNATURE_V5)

        private fun matches(actual: ByteArray, actualLength: Int, expected: ByteArray): Boolean {
            if (actualLength < expected.size) {
                return false
            }
            for (index in expected.indices) {
                if (actual[index] != expected[index]) {
                    return false
                }
            }
            return true
        }
    }
}
