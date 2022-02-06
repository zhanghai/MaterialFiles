/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.system.OsConstants
import me.zhanghai.android.files.hiddenapi.RestrictedHiddenApi
import me.zhanghai.android.files.util.lazyReflectedMethod
import java.io.Closeable
import java.io.FileDescriptor
import java.nio.channels.FileChannel

object NioUtilsCompat {
    @RestrictedHiddenApi
    private val newFileChannelMethod by lazyReflectedMethod(
        "java.nio.NioUtils", "newFileChannel", Closeable::class.java, FileDescriptor::class.java,
        Int::class.java
    )
    @RestrictedHiddenApi
    private val fileChannelImplOpenMethod by lazyReflectedMethod(
        "sun.nio.ch.FileChannelImpl", "open", FileDescriptor::class.java, String::class.java,
        Boolean::class.java, Boolean::class.java, Boolean::class.java, Any::class.java
    )

    fun newFileChannel(ioObject: Closeable, fd: FileDescriptor, flags: Int): FileChannel =
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.N until Build.VERSION_CODES.R) {
            // They broke O_RDONLY by assuming it's non-zero, but in fact it is zero.
            // https://android.googlesource.com/platform/libcore/+/nougat-release/luni/src/main/java/java/nio/NioUtils.java#63
            val readable = flags and OsConstants.O_ACCMODE != OsConstants.O_WRONLY
            val writable = flags and OsConstants.O_ACCMODE != OsConstants.O_RDONLY
            val append = flags and OsConstants.O_APPEND == OsConstants.O_APPEND
            fileChannelImplOpenMethod.invoke(
                null, fd, null, readable, writable, append, ioObject
            ) as FileChannel
        } else {
            newFileChannelMethod.invoke(null, ioObject, fd, flags) as FileChannel
        }
}
