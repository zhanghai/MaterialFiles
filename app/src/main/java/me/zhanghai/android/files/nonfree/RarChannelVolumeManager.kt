/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.nonfree

import com.github.junrar.Archive
import com.github.junrar.io.SeekableReadOnlyByteChannel
import com.github.junrar.volume.Volume
import com.github.junrar.volume.VolumeManager
import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.compat.withInitial
import java.io.EOFException
import java.io.IOException
import java.nio.ByteBuffer

internal class RarChannelVolumeManager(
    private val channel: SeekableByteChannel
) : VolumeManager {
    override fun nextVolume(archive: Archive, lastVolume: Volume?): Volume? =
        if (lastVolume == null) RarChannelVolume(archive, channel) else null
}

private class RarChannelVolume(
    private val archive: Archive,
    private val channel: SeekableByteChannel
) : Volume {
    private val delegateChannel = DelegateSeekableReadOnlyByteChannel(channel)

    @Throws(IOException::class)
    override fun getChannel(): SeekableReadOnlyByteChannel = delegateChannel

    override fun getLength(): Long =
        try {
            channel.size()
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }

    override fun getArchive(): Archive = archive
}

private class DelegateSeekableReadOnlyByteChannel(
    private val channel: SeekableByteChannel
) : SeekableReadOnlyByteChannel {
    private val SINGLE_BYTE_BUFFER = ThreadLocal::class.withInitial { ByteBuffer.allocate(1) }

    @Throws(IOException::class)
    override fun getPosition(): Long = channel.position()

    @Throws(IOException::class)
    override fun setPosition(pos: Long) {
        channel.position(pos)
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val buffer = SINGLE_BYTE_BUFFER.get()!!
        buffer.clear()
        while (true) {
            when (channel.read(buffer)) {
                -1 -> return -1
                0 -> continue
                else -> return buffer[0].toInt() and 0xFF
            }
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, off: Int, count: Int): Int {
        if (buffer.isEmpty()) {
            return 0
        }
        val byteBuffer = ByteBuffer.wrap(buffer, off, count)
        while (true) {
            val bytesRead = channel.read(byteBuffer)
            if (bytesRead == 0) {
                continue
            }
            return bytesRead
        }
    }

    @Throws(IOException::class)
    override fun readFully(buffer: ByteArray, count: Int): Int {
        require(count <= buffer.size) {
            "count > buffer.size: count = $count, buffer.size = ${buffer.size}"
        }
        if (count == 0) {
            return 0
        }
        val byteBuffer = ByteBuffer.wrap(buffer, 0, count)
        while (byteBuffer.hasRemaining()) {
            if (channel.read(byteBuffer) == -1) {
                throw EOFException()
            }
        }
        return count
    }

    @Throws(IOException::class)
    override fun close() {
        channel.close()
    }
}
