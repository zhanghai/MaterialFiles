/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.io.Closeable
import java.io.IOException
import java.io.OutputStream

fun OutputStream.withCloseable(closeable: Closeable): OutputStream =
    CloseableOutputStream(this, closeable)

private class CloseableOutputStream(
    outputStream: OutputStream,
    private val closeable: Closeable
) : DelegateOutputStream(outputStream) {
    @Throws(IOException::class)
    override fun close() {
        super.close()

        closeable.close()
    }
}
