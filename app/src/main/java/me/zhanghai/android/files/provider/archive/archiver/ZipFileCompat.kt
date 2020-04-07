/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import android.os.Build
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile as JavaZipFile

internal class ZipFileCompat(file: File, encoding: String) : Closeable {
    private var zipFile: ZipFile?
    private var javaZipFile: JavaZipFile?

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zipFile = ZipFile(file, encoding)
            javaZipFile = null
        } else {
            zipFile = null
            javaZipFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // The same charset logic as Apache Commons Compress.
                val charset = try {
                    Charset.forName(encoding)
                } catch (e: UnsupportedCharsetException) {
                    Charset.defaultCharset()
                }
                JavaZipFile(file, charset)
            } else {
                JavaZipFile(file)
            }
        }
    }

    val entries: Enumeration<ZipArchiveEntry>
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                zipFile!!.entries
            } else {
                val entries = javaZipFile!!.entries()
                object : Enumeration<ZipArchiveEntry> {
                    override fun hasMoreElements(): Boolean = entries.hasMoreElements()

                    override fun nextElement(): ZipArchiveEntry {
                        val entry = entries.nextElement()
                        return try {
                            ZipArchiveEntry(entry)
                        } catch (e: ZipException) {
                            e.printStackTrace()
                            UnparseableExtraZipArchiveEntry(entry)
                        }
                    }
                }
            }

    @Throws(IOException::class)
    fun getInputStream(entry: ZipArchiveEntry): InputStream? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zipFile!!.getInputStream(entry)
        } else {
            javaZipFile!!.getInputStream(entry)
        }

    @Throws(IOException::class)
    override fun close() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zipFile!!.close()
        } else {
            javaZipFile!!.close()
        }
    }

    private class UnparseableExtraZipArchiveEntry(entry: ZipEntry) : ZipArchiveEntry(entry.name) {
        init {
            time = entry.time
            setExtra()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lastModifiedTime = entry.lastModifiedTime
                lastAccessTime = entry.lastAccessTime
                creationTime = entry.creationTime
            }
            val crc = entry.crc
            if (crc in 0..0xFFFFFFFFL) {
                setCrc(entry.crc)
            }
            val size = entry.size
            if (size >= 0) {
                setSize(size)
            }
            compressedSize = entry.compressedSize
            method = entry.method
            comment = entry.comment
        }
    }
}
