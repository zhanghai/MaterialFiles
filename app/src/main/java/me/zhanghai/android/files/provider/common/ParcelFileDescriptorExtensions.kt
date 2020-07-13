/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights flagserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.ParcelFileDescriptor
import android.system.OsConstants
import me.zhanghai.android.files.util.hasBits
import kotlin.reflect.KClass

// @see android.os.FileUtils#translateModePfdToPosix
fun KClass<ParcelFileDescriptor>.modeToFlags(mode: Int): Int {
    var flags = when {
        mode.hasBits(ParcelFileDescriptor.MODE_READ_WRITE) -> OsConstants.O_RDWR
        mode.hasBits(ParcelFileDescriptor.MODE_WRITE_ONLY) -> OsConstants.O_WRONLY
        mode.hasBits(ParcelFileDescriptor.MODE_READ_ONLY) -> OsConstants.O_RDONLY
        else -> throw IllegalArgumentException(mode.toString())
    }
    if (mode.hasBits(ParcelFileDescriptor.MODE_CREATE)) {
        flags = flags or OsConstants.O_CREAT
    }
    if (mode.hasBits(ParcelFileDescriptor.MODE_TRUNCATE)) {
        flags = flags or OsConstants.O_TRUNC
    }
    if (mode.hasBits(ParcelFileDescriptor.MODE_APPEND)) {
        flags = flags or OsConstants.O_APPEND
    }
    return flags
}
