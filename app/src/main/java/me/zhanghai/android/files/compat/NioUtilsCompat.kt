/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.system.OsConstants
import me.zhanghai.java.reflected.ReflectedMethod
import java.io.Closeable
import java.io.FileDescriptor
import java.nio.channels.FileChannel

object NioUtilsCompat {
    @RestrictedHiddenApi
    private val newFileChannelMethod = ReflectedMethod<Nothing>(
        "java.nio.NioUtils", "newFileChannel", Closeable::class.java, FileDescriptor::class.java,
        Int::class.javaPrimitiveType
    )
    @RestrictedHiddenApi
    private val fileChannelImplOpenMethod = ReflectedMethod<Nothing>(
        "sun.nio.ch.FileChannelImpl", "open", FileDescriptor::class.java, String::class.java,
        Boolean::class.javaPrimitiveType, Boolean::class.javaPrimitiveType,
        Boolean::class.javaPrimitiveType, Any::class.java
    )

    fun newFileChannel(ioObject: Closeable, fd: FileDescriptor, flags: Int): FileChannel =
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.N..Build.VERSION_CODES.Q) {
            // They broke O_RDONLY by assuming it's non-zero, but in fact it is zero.
            // https://android.googlesource.com/platform/libcore/+/nougat-release/luni/src/main/java/java/nio/NioUtils.java#63
            val readable = flags and OsConstants.O_ACCMODE != OsConstants.O_WRONLY
            val writable = flags and OsConstants.O_ACCMODE != OsConstants.O_RDONLY
            val append = flags and OsConstants.O_APPEND == OsConstants.O_APPEND
            fileChannelImplOpenMethod.invoke(null, fd, null, readable, writable, append, ioObject)
        } else {
            newFileChannelMethod.invoke(null, ioObject, fd, flags)
        }
}
