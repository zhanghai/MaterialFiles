/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import com.github.junrar.exception.RarException
import org.apache.commons.compress.compressors.CompressorException
import java.io.IOException
import org.apache.commons.compress.archivers.ArchiveException as ApacheArchiveException

class ArchiveException : IOException {
    constructor(cause: ApacheArchiveException) : super(cause)

    constructor(cause: CompressorException) : super(cause)

    constructor(cause: RarException) : super(cause)
}
