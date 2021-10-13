/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMB2PacketHeader
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.mssmb2.copy.CopyChunkRequest
import com.hierynomus.mssmb2.copy.CopyChunkResponse
import com.hierynomus.protocol.commons.buffer.Buffer
import com.hierynomus.smb.SMBBuffer
import com.hierynomus.smbj.ProgressListener
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.share.File
import com.hierynomus.smbj.share.ShareAccessor
import com.hierynomus.smbj.share.StatusHandler
import java.io.InterruptedIOException

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/cd0162e4-7650-4293-8a2a-d696923203ef
@Throws(InterruptedIOException::class, SMBRuntimeException::class)
internal fun File.serverCopy(
    sourceOffset: Long,
    target: File,
    targetOffset: Long,
    length: Long,
    listener: ProgressListener?
) {
    val resumeKey = requestResumeKey()
    var maxChunkSize = 1024 * 1024L
    var maxNumberOfChunks = 16L
    var maxRequestSize = maxNumberOfChunks * maxChunkSize
    var totalBytesWritten = 0L
    while (true) {
        val chunksSourceOffset = sourceOffset + totalBytesWritten
        val chunksTargetOffset = targetOffset + totalBytesWritten
        val chunksLength = length - totalBytesWritten
        val chunks = createServerCopyChunks(
            chunksSourceOffset, chunksTargetOffset, chunksLength, maxNumberOfChunks, maxChunkSize,
            maxRequestSize
        )
        val request = CopyChunkRequest(resumeKey, chunks)
        val (responseHeader, response) = target.serverCopyChunk(request)
        if (responseHeader.statusCode == NtStatus.STATUS_INVALID_PARAMETER.value) {
            // @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/a1935898-6a86-4491-a8a3-942ec83b75a4
            maxNumberOfChunks = response.chunksWritten
            maxChunkSize = response.chunkBytesWritten.coerceAtMost(response.totalBytesWritten)
            maxRequestSize = response.totalBytesWritten
        } else {
            totalBytesWritten += response.totalBytesWritten
            listener?.onProgressChanged(totalBytesWritten, length)
            if (totalBytesWritten >= length) {
                break
            }
        }
        throwIfInterrupted()
    }
}

@Throws(InterruptedIOException::class)
private fun throwIfInterrupted() {
    if (Thread.interrupted()) {
        throw InterruptedIOException()
    }
}

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/5c03c9d6-15de-48a2-9835-8fb37f8a79d8
private const val FSCTL_SRV_REQUEST_RESUME_KEY = 0x00140078

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/594abdf9-c122-4951-aba9-577b714674c4
@Throws(SMBRuntimeException::class)
private fun File.requestResumeKey(): ByteArray? {
    val buffer = SMBBuffer(ioctl(FSCTL_SRV_REQUEST_RESUME_KEY, true, ByteArray(0), 0, 0))
    return buffer.readRawBytes(24)
}

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/676ae4b4-6758-4930-9f73-f0853fcad081
private fun createServerCopyChunks(
    sourceOffset: Long,
    targetOffset: Long,
    length: Long,
    maxNumberOfChunks: Long,
    maxChunkSize: Long,
    maxRequestSize: Long
): List<CopyChunkRequest.Chunk> {
    val chunks = mutableListOf<CopyChunkRequest.Chunk>()
    var numberOfChunks = 0L
    var requestSize = 0L
    val length = length.coerceAtMost(maxRequestSize)
    while (numberOfChunks < maxNumberOfChunks && requestSize < length) {
        val chunkSourceOffset = sourceOffset + requestSize
        val chunkTargetOffset = targetOffset + requestSize
        val chunkSize = maxChunkSize.coerceAtMost(length - requestSize)
        chunks.add(CopyChunkRequest.Chunk(chunkSourceOffset, chunkTargetOffset, chunkSize))
        ++numberOfChunks
        requestSize += chunkSize
    }
    return chunks
}

private val SERVER_COPY_CHUNK_STATUS_HANDLER = StatusHandler {
    it == NtStatus.STATUS_SUCCESS.value || it == NtStatus.STATUS_INVALID_PARAMETER.value
}

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/5c03c9d6-15de-48a2-9835-8fb37f8a79d8
@Throws(SMBRuntimeException::class)
private fun File.serverCopyChunk(
    request: CopyChunkRequest
): Pair<SMB2PacketHeader, CopyChunkResponse> {
    // FSCTL_SRV_COPYCHUNK and FSCTL_SRV_COPYCHUNK_WRITE FSCTL codes are used for performing server
    // side copy operations. These FSCTLs are issued by the application against an open handle to
    // the target file.
    val share = diskShare
    val buffer = SMBBuffer()
    request.write(buffer)
    //val readTimeout = share.readTimeout
    val readTimeout = share.treeConnect.config.readTimeout
    val ioctlResponse = ShareAccessor.ioctl(
        share, fileId, CopyChunkRequest.getCtlCode(), true, buffer.array(), buffer.rpos(),
        buffer.available(), SERVER_COPY_CHUNK_STATUS_HANDLER, readTimeout
    )
    if (ioctlResponse.error != null) {
        throw SMBApiException(ioctlResponse.header, "FSCTL_SRV_COPYCHUNK failed")
    }
    val response = CopyChunkResponse()
    try {
        response.read(SMBBuffer(ioctlResponse.outputBuffer))
    } catch (e: Buffer.BufferException) {
        throw SMBRuntimeException(e)
    }
    return ioctlResponse.header to response
}
