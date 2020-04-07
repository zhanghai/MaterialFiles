/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content

import android.net.Uri
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.common.ContentProviderFileAttributes
import org.threeten.bp.Instant

internal class ContentFileAttributesImpl(
    private val mimeType: String?,
    private val size: Long,
    private val uri: Uri
) : ContentProviderFileAttributes {
    override fun lastModifiedTime(): FileTime = FileTime.from(Instant.EPOCH)

    override fun lastAccessTime(): FileTime = lastModifiedTime()

    override fun creationTime(): FileTime = lastModifiedTime()

    override fun mimeType(): String? = mimeType

    override fun size(): Long = size

    override fun fileKey(): Uri = uri
}
