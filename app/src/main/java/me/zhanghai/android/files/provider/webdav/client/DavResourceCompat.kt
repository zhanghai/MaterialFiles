/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav.client

import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.DavResourceAccessor
import at.bitfire.dav4jvm.QuotedStringUtils
import at.bitfire.dav4jvm.ResponseCallback
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.exception.HttpException
import me.zhanghai.android.files.provider.common.DelegateOutputStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.Pipe
import okio.buffer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

@Throws(DavException::class, IOException::class)
fun DavResource.getCompat(accept: String, headers: Headers?): InputStream =
    get(accept, headers).also { checkStatus(it) }.body!!.byteStream()

@Throws(DavException::class, IOException::class)
fun DavResource.getRangeCompat(
    accept: String,
    offset: Long,
    size: Int,
    headers: Headers?
): InputStream =
    followRedirects {
        val request = Request.Builder().get().url(location)
        if (headers != null) {
            request.headers(headers)
        }
        request.header("Accept", accept)
        val lastIndex = offset + size - 1
        request.header("Range", "bytes=$offset-$lastIndex")
        httpClient.newCall(request.build()).execute()
    }
        .also {
            checkStatus(it)
            if (it.code != HttpURLConnection.HTTP_PARTIAL) {
                throw HttpException(it)
            }
        }
        .body!!.byteStream()

// This doesn't follow redirects since the request body is one-shot anyway.
@Throws(DavException::class, IOException::class)
fun DavResource.putCompat(
    ifETag: String? = null,
    ifScheduleTag: String? = null,
    ifNoneMatch: Boolean = false,
): OutputStream {
    val pipe = Pipe(DEFAULT_BUFFER_SIZE.toLong())
    val body = object : RequestBody() {
        override fun contentType(): MediaType? = null
        override fun isOneShot() = true
        override fun writeTo(sink: BufferedSink) {
            sink.writeAll(pipe.source)
        }
    }
    val builder = Request.Builder().put(body).url(location)
    if (ifETag != null) {
        // only overwrite specific version
        builder.header("If-Match", QuotedStringUtils.asQuotedString(ifETag))
    }
    if (ifScheduleTag != null) {
        // only overwrite specific version
        builder.header("If-Schedule-Tag-Match", QuotedStringUtils.asQuotedString(ifScheduleTag))
    }
    if (ifNoneMatch) {
        // don't overwrite anything existing
        builder.header("If-None-Match", "*")
    }
    var exceptionRef: IOException? = null
    var responseRef: Response? = null
    val callbackLatch = CountDownLatch(1)
    httpClient.newCall(builder.build()).enqueue(
        object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                exceptionRef = e
                callbackLatch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseRef = response
                callbackLatch.countDown()
            }
        }
    )
    return object : DelegateOutputStream(pipe.sink.buffer().outputStream()) {
        override fun close() {
            super.close()
            callbackLatch.await()
            exceptionRef?.let { throw it }
            checkStatus(responseRef!!)
        }
    }
}

enum class PatchSupport {
    NONE,
    APACHE,
    SABRE
}

@Throws(DavException::class, IOException::class)
fun DavResource.getPatchSupport(): PatchSupport {
    lateinit var patchSupport: PatchSupport
    options { davCapabilities, response ->
        patchSupport = when {
            response.headers["Server"]?.contains("Apache") == true &&
                "<http://apache.org/dav/propset/fs/1>" in davCapabilities ->
                PatchSupport.APACHE

            "sabredav-partialupdate" in davCapabilities -> PatchSupport.SABRE
            else -> PatchSupport.NONE
        }
    }
    return patchSupport
}

// https://sabre.io/dav/http-patch/
@Throws(DavException::class, IOException::class)
fun DavResource.patchCompat(
    buffer: ByteBuffer,
    offset: Long,
    ifETag: String? = null,
    ifScheduleTag: String? = null,
    ifNoneMatch: Boolean = false,
    callback: ResponseCallback
) {
    followRedirects {
        val builder = Request.Builder()
            .patch(buffer.toRequestBody("application/x-sabredav-partialupdate".toMediaType()))
            .url(location)
        val lastIndex = offset + buffer.remaining() - 1
        builder.header("X-Update-Range", "bytes=$offset-$lastIndex")
        if (ifETag != null) {
            // only overwrite specific version
            builder.header("If-Match", QuotedStringUtils.asQuotedString(ifETag))
        }
        if (ifScheduleTag != null) {
            // only overwrite specific version
            builder.header("If-Schedule-Tag-Match", QuotedStringUtils.asQuotedString(ifScheduleTag))
        }
        if (ifNoneMatch) {
            // don't overwrite anything existing
            builder.header("If-None-Match", "*")
        }
        httpClient.newCall(builder.build()).execute()
    }.use { response ->
        checkStatus(response)
        callback.onResponse(response)
    }
}

@Throws(DavException::class, IOException::class)
fun DavResource.putRangeCompat(
    buffer: ByteBuffer,
    offset: Long,
    ifETag: String? = null,
    ifScheduleTag: String? = null,
    ifNoneMatch: Boolean = false,
    callback: ResponseCallback
) {
    followRedirects {
        val builder = Request.Builder()
            .put(buffer.toRequestBody())
            .url(location)
        val lastIndex = offset + buffer.remaining() - 1
        builder.header("Range", "bytes=$offset-$lastIndex/*")
        if (ifETag != null) {
            // only overwrite specific version
            builder.header("If-Match", QuotedStringUtils.asQuotedString(ifETag))
        }
        if (ifScheduleTag != null) {
            // only overwrite specific version
            builder.header("If-Schedule-Tag-Match", QuotedStringUtils.asQuotedString(ifScheduleTag))
        }
        if (ifNoneMatch) {
            // don't overwrite anything existing
            builder.header("If-None-Match", "*")
        }
        httpClient.newCall(builder.build()).execute()
    }.use { response ->
        checkStatus(response)
        callback.onResponse(response)
    }
}

@Throws(HttpException::class)
private fun DavResource.checkStatus(response: Response) {
    DavResourceAccessor.checkStatus(this, response)
}

private fun DavResource.followRedirects(sendRequest: () -> Response): Response =
    DavResourceAccessor.followRedirects(this, sendRequest)

private fun ByteBuffer.toRequestBody(contentType: MediaType? = null): RequestBody {
    val contentLength = remaining().toLong()
    mark()
    return object : RequestBody() {
        override fun contentType() = contentType

        override fun contentLength(): Long = contentLength

        override fun writeTo(sink: BufferedSink) {
            reset()
            sink.write(this@toRequestBody)
        }
    }
}
