/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import java8.nio.channels.SeekableByteChannel
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.PosixFileMode
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.getLastModifiedTime
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.readAttributes
import me.zhanghai.android.files.provider.common.readSymbolicLinkByteString
import me.zhanghai.android.files.provider.common.size
import java.io.Closeable
import java.io.IOException

class ArchiveWriter @Throws(IOException::class) constructor(
    channel: SeekableByteChannel,
    format: Int,
    filter: Int,
    password: String?
) : Closeable {
    private val archive = WriteArchive(channel, format, filter, password)

    @Throws(IOException::class)
    fun write(file: Path, entryName: Path, intervalMillis: Long, listener: ((Long) -> Unit)?) {
        val name = entryName.toString()
        val lastModifiedTime = file.getLastModifiedTime(LinkOption.NOFOLLOW_LINKS)
        val lastAccessTime = null
        val creationTime = null
        val attributes = file.readAttributes(
            BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
        )
        val type = when {
            attributes is PosixFileAttributes -> attributes.type()
            attributes.isDirectory -> PosixFileType.DIRECTORY
            attributes.isSymbolicLink -> PosixFileType.SYMBOLIC_LINK
            else -> PosixFileType.REGULAR_FILE
        }
        val size = file.size(LinkOption.NOFOLLOW_LINKS)
        val posixAttributes = attributes as? PosixFileAttributes
        val owner = posixAttributes?.owner()
        val group = posixAttributes?.group()
        val mode = posixAttributes?.mode() ?: when {
            attributes.isDirectory -> PosixFileMode.DIRECTORY_DEFAULT
            attributes.isSymbolicLink -> PosixFileMode.SYMBOLIC_LINK_DEFAULT
            else -> PosixFileMode.FILE_DEFAULT
        }
        val symbolicLinkTarget = if (attributes.isSymbolicLink) {
            file.readSymbolicLinkByteString().toString()
        } else {
            null
        }
        archive.Entry(
            name, lastModifiedTime, lastAccessTime, creationTime, type, size, owner, group, mode,
            symbolicLinkTarget
        ).use { archive.writeEntry(it) }
        if (type == PosixFileType.REGULAR_FILE) {
            file.newInputStream(LinkOption.NOFOLLOW_LINKS).use { inputStream ->
                inputStream.copyTo(archive.newDataOutputStream(), intervalMillis, listener)
            }
        } else {
            listener?.invoke(attributes.size())
        }
    }

    @Throws(IOException::class)
    override fun close() {
        archive.close()
    }
}
