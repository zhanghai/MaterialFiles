/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.msdtyp.FileTime
import com.hierynomus.msfscc.fileinformation.FileAllInformation
import com.hierynomus.msfscc.fileinformation.FileIdFullDirectoryInformation
import com.hierynomus.msfscc.fileinformation.ShareInfo
import com.hierynomus.smbj.common.SMBRuntimeException

sealed class PathInformation

class FileInformation(
    val creationTime: FileTime,
    val lastAccessTime: FileTime,
    val lastWriteTime: FileTime,
    val changeTime: FileTime,
    val endOfFile: Long,
    val fileAttributes: Long,
    val fileId: Long
) : PathInformation()

@Throws(SMBRuntimeException::class)
fun FileIdFullDirectoryInformation.toFileInformation(): FileInformation =
    FileInformation(
        creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, fileAttributes, fileId
    )

fun FileAllInformation.toFileInformation(): FileInformation =
    FileInformation(
        basicInformation.creationTime, basicInformation.lastAccessTime,
        basicInformation.lastWriteTime, basicInformation.changeTime, standardInformation.endOfFile,
        basicInformation.fileAttributes, internalInformation.indexNumber
    )

class ShareInformation(
    val type: ShareType,
    val shareInfo: ShareInfo?
) : PathInformation()

enum class ShareType {
    DISK,
    PIPE,
    PRINTER,
}
