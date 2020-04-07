/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java.io.IOException
import java.io.InputStream

class RemoteInputStream : InputStream, Parcelable {
    private val localInputStream: InputStream?
    private val remoteInputStream: IRemoteInputStream?

    constructor(inputStream: InputStream) {
        localInputStream = inputStream
        remoteInputStream = null
    }

    @Throws(IOException::class)
    override fun read(): Int =
        if (remoteInputStream != null) {
            remoteInputStream.call { exception -> read(exception) }
        } else {
            localInputStream!!.read()
        }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
        if (remoteInputStream != null) {
            val remoteBuffer = ByteArray(length)
            val size = remoteInputStream.call { exception -> read2(remoteBuffer, exception) }
            if (size > 0) {
                remoteBuffer.copyInto(buffer, offset, 0, size)
            }
            size
        } else {
            localInputStream!!.read(buffer, offset, length)
        }

    @Throws(IOException::class)
    override fun skip(size: Long): Long =
        if (remoteInputStream != null) {
            remoteInputStream.call { exception -> skip(size, exception) }
        } else {
            localInputStream!!.skip(size)
        }

    @Throws(IOException::class)
    override fun available(): Int =
        if (remoteInputStream != null) {
            remoteInputStream.call { exception -> available(exception) }
        } else {
            localInputStream!!.available()
        }

    @Throws(IOException::class)
    override fun close() {
        if (remoteInputStream != null) {
            remoteInputStream.call { exception -> close(exception) }
        } else {
            localInputStream!!.close()
        }
    }

    private class Stub(private val mInputStream: InputStream) : IRemoteInputStream.Stub() {
        override fun read(exception: ParcelableException): Int =
            tryRun(exception) { mInputStream.read() } ?: 0

        override fun read2(buffer: ByteArray, exception: ParcelableException): Int =
            tryRun(exception) { mInputStream.read(buffer) } ?: 0

        override fun skip(size: Long, exception: ParcelableException): Long =
            tryRun(exception) { mInputStream.skip(size) } ?: 0

        override fun available(exception: ParcelableException): Int =
            tryRun(exception) { mInputStream.available() } ?: 0

        override fun close(exception: ParcelableException) {
            tryRun(exception) { mInputStream.close() }
        }
    }

    private constructor(source: Parcel) {
        localInputStream = null
        remoteInputStream = IRemoteInputStream.Stub.asInterface(source.readStrongBinder())
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (remoteInputStream != null) {
            dest.writeStrongBinder(remoteInputStream.asBinder())
        } else {
            dest.writeStrongBinder(Stub(localInputStream!!).asBinder())
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteInputStream> {
            override fun createFromParcel(source: Parcel): RemoteInputStream =
                RemoteInputStream(source)

            override fun newArray(size: Int): Array<RemoteInputStream?> = arrayOfNulls(size)
        }
    }
}

fun InputStream.toRemote(): RemoteInputStream = RemoteInputStream(this)
