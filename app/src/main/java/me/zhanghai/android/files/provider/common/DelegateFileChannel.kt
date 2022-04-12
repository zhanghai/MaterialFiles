/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.channels.FileChannel
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileLock
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

open class DelegateFileChannel(private val channel: FileChannel) : FileChannel() {
    @Throws(IOException::class)
    override fun read(dst: ByteBuffer): Int = channel.read(dst)

    @Throws(IOException::class)
    override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long =
        channel.read(dsts, offset, length)

    @Throws(IOException::class)
    override fun write(src: ByteBuffer): Int = channel.write(src)

    @Throws(IOException::class)
    override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long =
        channel.write(srcs, offset, length)

    @Throws(IOException::class)
    override fun position(): Long = channel.position()

    @Throws(IOException::class)
    override fun position(newPosition: Long): FileChannel {
        channel.position(newPosition)
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long = channel.size()

    @Throws(IOException::class)
    override fun truncate(size: Long): FileChannel {
        channel.truncate(size)
        return this
    }

    @Throws(IOException::class)
    override fun force(metaData: Boolean) {
        channel.force(metaData)
    }

    @Throws(IOException::class)
    override fun transferTo(position: Long, count: Long, target: WritableByteChannel): Long =
        channel.transferTo(position, count, target)

    @Throws(IOException::class)
    override fun transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long =
        channel.transferFrom(src, position, count)

    @Throws(IOException::class)
    override fun read(dst: ByteBuffer, position: Long): Int = channel.read(dst, position)

    @Throws(IOException::class)
    override fun write(src: ByteBuffer, position: Long): Int = channel.write(src, position)

    @Throws(IOException::class)
    override fun map(mode: MapMode, position: Long, size: Long): MappedByteBuffer =
        channel.map(mode, position, size)

    @Throws(IOException::class)
    override fun lock(position: Long, size: Long, shared: Boolean): FileLock =
        channel.lock(position, size, shared)

    @Throws(IOException::class)
    override fun tryLock(position: Long, size: Long, shared: Boolean): FileLock? =
        channel.tryLock(position, size, shared)

    @Throws(IOException::class)
    override fun implCloseChannel() {
        channel.close()
    }
}
