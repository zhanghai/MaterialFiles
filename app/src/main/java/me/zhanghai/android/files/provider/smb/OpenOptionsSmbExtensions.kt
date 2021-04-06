/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import me.zhanghai.android.files.provider.common.OpenOptions
import me.zhanghai.android.files.util.enumSetOf

internal fun OpenOptions.toSmbDesiredAccess(): Set<AccessMask> =
    enumSetOf<AccessMask>().apply {
        if (read) {
            this += AccessMask.GENERIC_READ
        }
        if (write) {
            this += AccessMask.GENERIC_WRITE
        }
    }

internal fun OpenOptions.toSmbFileAttributes(): Set<FileAttributes> =
    enumSetOf<FileAttributes>().apply {
        if (sparse) {
            this += FileAttributes.FILE_ATTRIBUTE_SPARSE_FILE
        }
    }

internal fun OpenOptions.toSmbShareAccess(): Set<SMB2ShareAccess> = SMB2ShareAccess.ALL

internal fun OpenOptions.toSmbCreateDisposition(): SMB2CreateDisposition =
    when {
        createNew -> SMB2CreateDisposition.FILE_CREATE
        create && truncateExisting -> SMB2CreateDisposition.FILE_OVERWRITE_IF
        create -> SMB2CreateDisposition.FILE_OPEN_IF
        truncateExisting -> SMB2CreateDisposition.FILE_OVERWRITE
        else -> SMB2CreateDisposition.FILE_OPEN
    }

internal fun OpenOptions.toSmbCreateOptions(): Set<SMB2CreateOptions> =
    enumSetOf<SMB2CreateOptions>().apply {
        if (sync || dsync) {
            this += SMB2CreateOptions.FILE_WRITE_THROUGH
        }
        if (deleteOnClose) {
            this += SMB2CreateOptions.FILE_DELETE_ON_CLOSE
        }
        if (noFollowLinks || createNew) {
            this += SMB2CreateOptions.FILE_OPEN_REPARSE_POINT
        }
    }
