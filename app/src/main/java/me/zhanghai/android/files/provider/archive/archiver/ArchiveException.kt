/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

//#ifdef NONFREE
import com.github.junrar.exception.RarException
//#endif
import org.apache.commons.compress.compressors.CompressorException
import java.io.IOException
import org.apache.commons.compress.archivers.ArchiveException as ApacheArchiveException

class ArchiveException : IOException {
    constructor(cause: ApacheArchiveException) : super(cause)

    constructor(cause: CompressorException) : super(cause)

//#ifdef NONFREE
    constructor(cause: RarException) : super(cause)
//#endif
}
