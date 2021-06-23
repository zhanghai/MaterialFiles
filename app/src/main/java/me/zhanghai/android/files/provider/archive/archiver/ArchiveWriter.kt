/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import android.os.Build
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.compat.toJavaSeekableByteChannel
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.getLastModifiedTime
import me.zhanghai.android.files.provider.common.isDirectory
import me.zhanghai.android.files.provider.common.isRegularFile
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.provider.common.readAttributes
import me.zhanghai.android.files.provider.common.readSymbolicLinkByteString
import me.zhanghai.android.files.provider.common.size
import me.zhanghai.android.files.provider.common.toInt
import me.zhanghai.android.files.util.lazyReflectedField
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarConstants
import org.apache.commons.compress.archivers.zip.UnixStat
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.OutputStream

class ArchiveWriter(
    archiveType: String,
    compressorType: String?,
    channel: SeekableByteChannel
) : Closeable {
    private val archiveOutputStream: ArchiveOutputStream

    init {
        when (archiveType) {
            ArchiveStreamFactory.SEVEN_Z -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    throw IOException(UnsupportedOperationException("SevenZOutputFile"))
                }
                archiveOutputStream = SevenZArchiveOutputStream(
                    SevenZOutputFile(channel.toJavaSeekableByteChannel())
                )
            }
            else -> {
                var successful = false
                var outputStream: OutputStream? = null
                var compressorOutputStream: OutputStream? = null
                try {
                    outputStream = channel.newOutputStream().buffered()
                    compressorOutputStream = if (compressorType != null) {
                        compressorStreamFactory.createCompressorOutputStream(
                            compressorType, outputStream
                        )
                    } else {
                        outputStream
                    }
                    // Use the platform default encoding (which is UTF-8) instead of the user-set
                    // one, because that one is for reading archives instead of creating.
                    archiveOutputStream = archiveStreamFactory.createArchiveOutputStream(
                        archiveType, compressorOutputStream
                    )
                    successful = true
                } catch (e: org.apache.commons.compress.archivers.ArchiveException) {
                    throw ArchiveException(e)
                } catch (e: CompressorException) {
                    throw ArchiveException(e)
                } finally {
                    if (!successful) {
                        compressorOutputStream?.close()
                        outputStream?.close()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun write(file: Path, entryName: Path, intervalMillis: Long, listener: ((Long) -> Unit)?) {
        val entry = archiveOutputStream.createArchiveEntry(PathFile(file), entryName.toString())
        val attributes = file.readAttributes(
            BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
        )
        val writeData = when {
            attributes.isRegularFile -> true
            attributes.isDirectory -> false
            attributes.isSymbolicLink ->
                when (entry) {
                    is ZipArchiveEntry -> {
                        entry.unixMode = UnixStat.LINK_FLAG or UnixStat.DEFAULT_LINK_PERM
                        true
                    }
                    is TarArchiveEntry -> {
                        tarArchiveEntryLinkFlagsField.setByte(entry, TarConstants.LF_SYMLINK)
                        entry.linkName = file.readSymbolicLinkByteString().toString()
                        false
                    }
                    else -> throw IOException(UnsupportedOperationException("symbolic link"))
                }
            else -> throw IOException(UnsupportedOperationException("type"))
        }
        if (entry is TarArchiveEntry && attributes is PosixFileAttributes) {
            attributes.mode()?.let { entry.mode = it.toInt() }
            val owner = attributes.owner()
            if (owner != null) {
                entry.userId = owner.id
                owner.name?.let { entry.userName = it }
            }
            val group = attributes.group()
            if (group != null) {
                entry.groupId = group.id
                group.name?.let { entry.groupName = it }
            }
        }
        archiveOutputStream.putArchiveEntry(entry)
        var isListenerNotified = false
        if (writeData) {
            if (attributes.isSymbolicLink) {
                val target = file.readSymbolicLinkByteString().borrowBytes()
                archiveOutputStream.write(target)
            } else {
                file.newInputStream(LinkOption.NOFOLLOW_LINKS).use { inputStream ->
                    inputStream.copyTo(archiveOutputStream, intervalMillis, listener)
                }
                isListenerNotified = true
            }
        }
        archiveOutputStream.closeArchiveEntry()
        if (!isListenerNotified) {
            listener?.invoke(attributes.size())
        }
    }

    @Throws(IOException::class)
    override fun close() {
        archiveOutputStream.finish()
        archiveOutputStream.close()
    }

    private class SevenZArchiveOutputStream(
        private val file: SevenZOutputFile
    ) : ArchiveOutputStream() {
        @Throws(IOException::class)
        override fun createArchiveEntry(file: File, entryName: String): ArchiveEntry =
            this.file.createArchiveEntry(file, entryName)

        @Throws(IOException::class)
        override fun putArchiveEntry(entry: ArchiveEntry) {
            file.putArchiveEntry(entry)
        }

        @Throws(IOException::class)
        override fun write(b: Int) {
            file.write(b)
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray) {
            file.write(b)
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray, off: Int, len: Int) {
            file.write(b, off, len)
        }

        @Throws(IOException::class)
        override fun closeArchiveEntry() {
            file.closeArchiveEntry()
        }

        @Throws(IOException::class)
        override fun finish() {
            file.finish()
        }

        @Throws(IOException::class)
        override fun close() {
            file.close()
        }
    }

    // {@link ArchiveOutputStream#createArchiveEntry(File, String)} doesn't actually need a real
    // file.
    private class PathFile(private val path: Path) : File(path.toString()) {
        override fun isDirectory(): Boolean = path.isDirectory(LinkOption.NOFOLLOW_LINKS)

        override fun isFile(): Boolean = path.isRegularFile(LinkOption.NOFOLLOW_LINKS)

        override fun lastModified(): Long =
            try {
                path.getLastModifiedTime(LinkOption.NOFOLLOW_LINKS).toMillis()
            } catch (e: IOException) {
                e.printStackTrace()
                0
            }

        override fun length(): Long =
            try {
                path.size(LinkOption.NOFOLLOW_LINKS)
            } catch (e: IOException) {
                e.printStackTrace()
                0
            }
    }

    companion object {
        private val compressorStreamFactory = CompressorStreamFactory()
        private val archiveStreamFactory = ArchiveStreamFactory()

        private val tarArchiveEntryLinkFlagsField by lazyReflectedField(
            TarArchiveEntry::class.java, "linkFlag"
        )
    }
}
