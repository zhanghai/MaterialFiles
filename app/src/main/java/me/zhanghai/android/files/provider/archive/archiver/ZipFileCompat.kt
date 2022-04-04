/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.channels.SeekableByteChannel
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile as JavaZipFile

internal class ZipFileCompat : Closeable {
    @RequiresApi(Build.VERSION_CODES.N)
    private val zipFile: ZipFile?
    private val javaZipFile: JavaZipFile?

    @RequiresApi(Build.VERSION_CODES.N)
    constructor(channel: SeekableByteChannel, encoding: String?) {
        zipFile = ZipFile(channel, encoding)
        javaZipFile = null
    }

    constructor(file: File) {
        @SuppressLint("NewApi")
        zipFile = null
        javaZipFile = JavaZipFile(file)
    }

    val entries: Enumeration<ZipArchiveEntry>
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            zipFile!!.getInputStream(entry)
        } else {
            javaZipFile!!.getInputStream(entry)
        }

    @Throws(IOException::class)
    override fun close() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
