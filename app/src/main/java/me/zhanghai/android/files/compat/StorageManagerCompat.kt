/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.files.util.lazyReflectedMethod
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val getVolumeListMethod by lazyReflectedMethod(StorageManager::class.java, "getVolumeList")

val StorageManager.storageVolumesCompat: List<StorageVolume>
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            storageVolumes
        } else {
            @Suppress("UNCHECKED_CAST")
            (getVolumeListMethod.invoke(this) as Array<StorageVolume>).toList()
        }

// Thanks to fython for https://gist.github.com/fython/924f8d9019bca75d22de116bb69a54a1
@Throws(IOException::class)
fun StorageManager.openProxyFileDescriptorCompat(
    mode: Int,
    callback: ProxyFileDescriptorCallbackCompat,
    handler: Handler
): ParcelFileDescriptor =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        openProxyFileDescriptor(mode, callback.toProxyFileDescriptorCallback(), handler)
    } else {
        // TODO: Support other modes?
        if (mode != ParcelFileDescriptor.MODE_READ_ONLY) {
            throw UnsupportedOperationException("mode $mode")
        }
        val pfds = ParcelFileDescriptor.createReliablePipe()
        PipeWriter(pfds[1], callback, handler).start()
        pfds[0]
    }

private class PipeWriter (
    private val pfd: ParcelFileDescriptor,
    private val callback: ProxyFileDescriptorCallbackCompat,
    private val handler: Handler
) : Thread("StorageManagerCompat.PipeWriter-${id.getAndIncrement()}") {
    override fun run() {
        try {
            ParcelFileDescriptor.AutoCloseOutputStream(pfd).use { outputStream ->
                var offset = 0L
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val size = runBlocking {
                        callback.awaitOnRead(offset, buffer.size, buffer, handler)
                    }
                    if (size == 0) {
                        break
                    }
                    offset += size.toLong()
                    outputStream.write(buffer, 0, size)
                }
                runBlocking { callback.awaitOnRelease(handler) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                pfd.closeWithError(e.message)
            } catch (e2: IOException) {
                e2.printStackTrace()
            }
        }
    }

    companion object {
        private val id = AtomicInteger()
    }
}

private suspend fun ProxyFileDescriptorCallbackCompat.awaitOnRead(
    offset: Long,
    size: Int,
    data: ByteArray,
    handler: Handler
): Int =
    suspendCoroutine { continuation ->
        handler.post {
            val readSize = try {
                onRead(offset, size, data)
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
                return@post
            }
            continuation.resume(readSize)
        }
    }

private suspend fun ProxyFileDescriptorCallbackCompat.awaitOnRelease(handler: Handler) {
    suspendCoroutine<Unit> { continuation ->
        handler.post {
            try {
                onRelease()
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
                return@post
            }
            continuation.resume(Unit)
        }
    }
}
