/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.provider.common.force
import java.io.IOException
import java.nio.ByteBuffer

class RemoteSeekableByteChannel : ForceableChannel, SeekableByteChannel, Parcelable {
    private val localChannel: SeekableByteChannel?
    private val remoteChannel: IRemoteSeekableByteChannel?

    @Volatile
    private var isRemoteClosed = false

    constructor(channel: SeekableByteChannel) {
        localChannel = channel
        remoteChannel = null
    }

    @Throws(IOException::class)
    override fun read(destination: ByteBuffer): Int =
        if (remoteChannel != null) {
            val destinationBytes = ByteArray(destination.remaining())
            val size = remoteChannel.call { exception -> read(destinationBytes, exception) }
            if (size > 0) {
                destination.put(destinationBytes, 0, size)
            }
            size
        } else {
            localChannel!!.read(destination)
        }

    @Throws(IOException::class)
    override fun write(source: ByteBuffer): Int =
        if (remoteChannel != null) {
            val oldPosition = source.position()
            val sourceBytes = ByteArray(source.remaining())
            source.get(sourceBytes)
            source.position(oldPosition)
            val size = remoteChannel.call { exception -> write(sourceBytes, exception) }
            source.position(oldPosition + size)
            size
        } else {
            localChannel!!.write(source)
        }

    @Throws(IOException::class)
    override fun position(): Long =
        if (remoteChannel != null) {
            remoteChannel.call { exception -> position(exception) }
        } else {
            localChannel!!.position()
        }

    @Throws(IOException::class)
    override fun position(newPosition: Long): SeekableByteChannel {
        if (remoteChannel != null) {
            remoteChannel.call { exception -> position2(newPosition, exception) }
        } else {
            localChannel!!.position(newPosition)
        }
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long =
        if (remoteChannel != null) {
            remoteChannel.call { exception -> size(exception) }
        } else {
            localChannel!!.size()
        }

    @Throws(IOException::class)
    override fun truncate(size: Long): SeekableByteChannel {
        if (remoteChannel != null) {
            remoteChannel.call { exception -> truncate(size, exception) }
        } else {
            return localChannel!!.truncate(size)
        }
        return this
    }

    @Throws(IOException::class)
    override fun force(metaData: Boolean) {
        if (remoteChannel != null) {
            remoteChannel.call { exception -> force(metaData, exception) }
        } else {
            localChannel!!.force(metaData)
        }
    }

    override fun isOpen(): Boolean =
        if (remoteChannel != null) {
            !isRemoteClosed
        } else {
            localChannel!!.isOpen
        }

    @Throws(IOException::class)
    override fun close() {
        if (remoteChannel != null) {
            remoteChannel.call { exception -> close(exception) }
            isRemoteClosed = true
        } else {
            localChannel!!.close()
        }
    }

    private class Stub(
        private val channel: SeekableByteChannel
    ) : IRemoteSeekableByteChannel.Stub() {
        override fun read(destination: ByteArray, exception: ParcelableException): Int =
            tryRun(exception) { channel.read(ByteBuffer.wrap(destination)) } ?: 0

        override fun write(source: ByteArray, exception: ParcelableException): Int =
            tryRun(exception) { channel.write(ByteBuffer.wrap(source)) } ?: 0

        override fun position(exception: ParcelableException): Long =
            tryRun(exception) { channel.position() } ?: 0

        override fun position2(newPosition: Long, exception: ParcelableException) {
            tryRun(exception) { channel.position(newPosition) }
        }

        override fun size(exception: ParcelableException): Long =
            tryRun(exception) { channel.size() } ?: 0

        override fun truncate(size: Long, exception: ParcelableException) {
            tryRun(exception) { channel.truncate(size) }
        }

        override fun force(metaData: Boolean, exception: ParcelableException) {
            tryRun(exception) { channel.force(metaData) }
        }

        override fun close(exception: ParcelableException) {
            tryRun(exception) { channel.close() }
        }
    }

    private constructor(source: Parcel) {
        localChannel = null
        remoteChannel = IRemoteSeekableByteChannel.Stub.asInterface(source.readStrongBinder())
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (remoteChannel != null) {
            dest.writeStrongBinder(remoteChannel.asBinder())
        } else {
            dest.writeStrongBinder(Stub(localChannel!!).asBinder())
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteSeekableByteChannel> {
            override fun createFromParcel(source: Parcel): RemoteSeekableByteChannel =
                RemoteSeekableByteChannel(source)

            override fun newArray(size: Int): Array<RemoteSeekableByteChannel?> = arrayOfNulls(size)
        }
    }
}

fun SeekableByteChannel.toRemote(): RemoteSeekableByteChannel = RemoteSeekableByteChannel(this)
