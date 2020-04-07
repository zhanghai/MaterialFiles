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

open class DelegateFileChannel(private val fileChannel: FileChannel) : FileChannel() {
    @Throws(IOException::class)
    override fun read(dst: ByteBuffer): Int = fileChannel.read(dst)

    @Throws(IOException::class)
    override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long =
        fileChannel.read(dsts, offset, length)

    @Throws(IOException::class)
    override fun write(src: ByteBuffer): Int = fileChannel.write(src)

    @Throws(IOException::class)
    override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long =
        fileChannel.write(srcs, offset, length)

    @Throws(IOException::class)
    override fun position(): Long = fileChannel.position()

    @Throws(IOException::class)
    override fun position(newPosition: Long): DelegateFileChannel {
        fileChannel.position(newPosition)
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long = fileChannel.size()

    @Throws(IOException::class)
    override fun truncate(size: Long): DelegateFileChannel {
        fileChannel.truncate(size)
        return this
    }

    @Throws(IOException::class)
    override fun force(metaData: Boolean) {
        fileChannel.force(metaData)
    }

    @Throws(IOException::class)
    override fun transferTo(position: Long, count: Long, target: WritableByteChannel): Long =
        fileChannel.transferTo(position, count, target)

    @Throws(IOException::class)
    override fun transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long =
        fileChannel.transferFrom(src, position, count)

    @Throws(IOException::class)
    override fun read(dst: ByteBuffer, position: Long): Int = fileChannel.read(dst, position)

    @Throws(IOException::class)
    override fun write(src: ByteBuffer, position: Long): Int = fileChannel.write(src, position)

    @Throws(IOException::class)
    override fun map(mode: MapMode, position: Long, size: Long): MappedByteBuffer =
        fileChannel.map(mode, position, size)

    @Throws(IOException::class)
    override fun lock(position: Long, size: Long, shared: Boolean): FileLock =
        fileChannel.lock(position, size, shared)

    @Throws(IOException::class)
    override fun tryLock(position: Long, size: Long, shared: Boolean): FileLock? =
        fileChannel.tryLock(position, size, shared)

    @Throws(IOException::class)
    override fun implCloseChannel() {
        fileChannel.close()
    }
}
