/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/596a1078-e883-4972-9bbc-49e60bebca55
object NtStatuses {
    const val STATUS_NOT_A_REPARSE_POINT = 0xC0000275L
    const val STATUS_IO_REPARSE_TAG_INVALID = 0xC0000276
    const val STATUS_IO_REPARSE_TAG_MISMATCH = 0xC0000277L
}
