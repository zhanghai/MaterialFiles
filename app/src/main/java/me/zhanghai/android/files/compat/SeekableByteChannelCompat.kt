/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import androidx.annotation.RequiresApi
import java8.nio.channels.SeekableByteChannel
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel as JavaSeekableByteChannel

@RequiresApi(Build.VERSION_CODES.N)
fun SeekableByteChannel.toJavaSeekableByteChannel(): JavaSeekableByteChannel =
    // java8.nio.channels.FileChannel extends from java.nio.channels.FileChannel, so in that case
    // the current object may already be implementing java.nio.channels.SeekableByteChannel.
    this as? JavaSeekableByteChannel ?: DelegateJavaSeekableByteChannel(this)

@RequiresApi(Build.VERSION_CODES.N)
private class DelegateJavaSeekableByteChannel(
    private val channel: SeekableByteChannel
) : JavaSeekableByteChannel {
    @Throws(IOException::class)
    override fun read(dst: ByteBuffer): Int = channel.read(dst)

    @Throws(IOException::class)
    override fun write(src: ByteBuffer): Int = channel.write(src)

    @Throws(IOException::class)
    override fun position(): Long = channel.position()

    @Throws(IOException::class)
    override fun position(newPosition: Long): DelegateJavaSeekableByteChannel {
        channel.position(newPosition)
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long = channel.size()

    @Throws(IOException::class)
    override fun truncate(size: Long): DelegateJavaSeekableByteChannel {
        channel.truncate(size)
        return this
    }

    override fun isOpen(): Boolean = channel.isOpen

    @Throws(IOException::class)
    override fun close() {
        channel.close()
    }
}
