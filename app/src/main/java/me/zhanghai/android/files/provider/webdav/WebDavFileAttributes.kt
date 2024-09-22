/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav

import android.os.Parcelable
import at.bitfire.dav4jvm.Response
import java.time.Instant
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.provider.common.AbstractBasicFileAttributes
import me.zhanghai.android.files.provider.common.BasicFileType
import me.zhanghai.android.files.provider.common.EPOCH
import me.zhanghai.android.files.provider.common.FileTimeParceler
import me.zhanghai.android.files.provider.webdav.client.creationTime
import me.zhanghai.android.files.provider.webdav.client.isDirectory
import me.zhanghai.android.files.provider.webdav.client.isSymbolicLink
import me.zhanghai.android.files.provider.webdav.client.lastModifiedTime
import me.zhanghai.android.files.provider.webdav.client.size

@Parcelize
internal data class WebDavFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val lastAccessTime: @WriteWith<FileTimeParceler> FileTime,
    override val creationTime: @WriteWith<FileTimeParceler> FileTime,
    override val type: BasicFileType,
    override val size: Long,
    override val fileKey: Parcelable
) : AbstractBasicFileAttributes() {
    companion object {
        fun from(response: Response, path: WebDavPath): WebDavFileAttributes =
            when {
                response.isSuccess() -> {
                    val lastModifiedTime = FileTime.from(response.lastModifiedTime ?: Instant.EPOCH)
                    val lastAccessTime = lastModifiedTime
                    val creationTime =
                        response.creationTime?.let { FileTime.from(it) } ?: lastModifiedTime
                    val type = if (response.isDirectory) {
                        BasicFileType.DIRECTORY
                    } else {
                        BasicFileType.REGULAR_FILE
                    }
                    val size = response.size
                    val fileKey = path
                    WebDavFileAttributes(
                        lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey
                    )
                }
                response.isSymbolicLink -> {
                    val lastModifiedTime = FileTime::class.EPOCH
                    val lastAccessTime = lastModifiedTime
                    val creationTime = lastModifiedTime
                    val type = BasicFileType.SYMBOLIC_LINK
                    val size = 0L
                    val fileKey = path
                    WebDavFileAttributes(
                        lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey
                    )
                }
                else -> error(response)
            }
        }
}
