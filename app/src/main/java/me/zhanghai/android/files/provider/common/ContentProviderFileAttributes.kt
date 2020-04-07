/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.file.MimeType

interface ContentProviderFileAttributes : BasicFileAttributes {
    fun mimeType(): String?

    override fun isRegularFile(): Boolean = !isDirectory

    override fun isDirectory(): Boolean = mimeType() == MimeType.DIRECTORY.value

    override fun isSymbolicLink(): Boolean = false

    override fun isOther(): Boolean = false
}
