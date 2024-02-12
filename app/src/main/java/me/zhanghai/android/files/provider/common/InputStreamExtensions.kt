/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream

// Can handle ProgressCopyOption.
@Throws(IOException::class)
fun InputStream.copyTo(
    outputStream: OutputStream,
    intervalMillis: Long,
    listener: ((Long) -> Unit)?
) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var lastProgressMillis = System.currentTimeMillis()
    var copiedSize = 0L
    while (true) {
        val readSize = read(buffer)
        if (readSize == -1) {
            break
        }
        outputStream.write(buffer, 0, readSize)
        copiedSize += readSize.toLong()
        throwIfInterrupted()
        val currentTimeMillis = System.currentTimeMillis()
        if (listener != null && currentTimeMillis >= lastProgressMillis + intervalMillis) {
            listener(copiedSize)
            lastProgressMillis = currentTimeMillis
            copiedSize = 0
        }
    }
    listener?.invoke(copiedSize)
}

@Throws(IOException::class)
fun InputStream.readFully(buffer: ByteArray, offset: Int, length: Int): Int {
    var totalReadSize = 0
    while (totalReadSize < length) {
        val readSize = read(buffer, offset + totalReadSize, length - totalReadSize)
        if (readSize == -1) {
            break
        }
        totalReadSize += readSize
    }
    return totalReadSize
}

@Throws(InterruptedIOException::class)
private fun throwIfInterrupted() {
    if (Thread.interrupted()) {
        throw InterruptedIOException()
    }
}
