/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcelable
import java8.nio.file.attribute.FileTime

abstract class AbstractContentProviderFileAttributes : ContentProviderFileAttributes, Parcelable {
    protected abstract val lastModifiedTime: FileTime
    protected abstract val mimeType: String?
    protected abstract val size: Long
    protected abstract val fileKey: Parcelable

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun mimeType(): String? = mimeType

    override fun size(): Long = size

    override fun fileKey(): Parcelable = fileKey
}
