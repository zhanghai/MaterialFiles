/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mssmb2.SMB2CompletionFilter
import com.hierynomus.mssmb2.SMB2Dialect
import com.hierynomus.mssmb2.SMB2FileId
import com.hierynomus.mssmb2.SMB2MessageCommandCode
import com.hierynomus.mssmb2.SMB2MultiCreditPacket
import com.hierynomus.mssmb2.SMB2Packet
import com.hierynomus.mssmb2.messages.SMB2ChangeNotifyResponse
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.protocol.transport.TransportException
import com.hierynomus.smb.SMBBuffer
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.Share
import java.util.concurrent.Future

@Throws(SMBRuntimeException::class)
fun Directory.changeNotifyAsync(
    flags: Set<SMB2ChangeNotifyFlags>,
    completionFilter: Set<SMB2CompletionFilter>
): Future<SMB2ChangeNotifyResponse> {
    return diskShare.changeNotifyAsync(fileId, flags, completionFilter)
}

@Throws(SMBRuntimeException::class)
private fun Share.changeNotifyAsync(
    fileId: SMB2FileId,
    flags: Set<SMB2ChangeNotifyFlags>,
    completionFilter: Set<SMB2CompletionFilter>
): Future<SMB2ChangeNotifyResponse> {
    val treeConnect = treeConnect
    val session = treeConnect.session
    val connection = session.connection
    val negotiatedProtocol = connection.negotiatedProtocol
    val dialect = negotiatedProtocol.dialect
    val sessionId = session.sessionId
    val treeId = treeConnect.treeId
    val transactBufferSize = connection.config.transactBufferSize
        .coerceAtMost(negotiatedProtocol.maxTransactSize)
    val request = SMB2ChangeNotifyRequest(
        dialect, sessionId, treeId, fileId, flags, completionFilter, transactBufferSize
    )
    return send(request)
}

// com.hierynomus.mssmb2.messages.SMB2ChangeNotifyRequest is not a SMB2MultiCreditPacket.
class SMB2ChangeNotifyRequest(
    smbDialect: SMB2Dialect,
    sessionId: Long,
    treeId: Long,
    private val fileId: SMB2FileId,
    private val flags: Set<SMB2ChangeNotifyFlags>,
    private val completionFilter: Set<SMB2CompletionFilter>,
    maxBufferSize: Int
) : SMB2MultiCreditPacket(
    32, smbDialect, SMB2MessageCommandCode.SMB2_CHANGE_NOTIFY, sessionId, treeId, maxBufferSize
) {
    override fun writeTo(buffer: SMBBuffer) {
        // StructureSize (2 bytes)
        buffer.putUInt16(structureSize)
        // Flags (2 bytes)
        buffer.putUInt16(EnumWithValue.EnumUtils.toLong(flags).toInt())
        // OutputBufferLength (4 bytes)
        buffer.putUInt32(
            maxPayloadSize.toLong()
                .coerceAtMost(SMB2Packet.SINGLE_CREDIT_PAYLOAD_SIZE.toLong() * creditsAssigned)
        )
        // FileId (16 bytes)
        fileId.write(buffer)
        // CompletionFilter (4 bytes)
        buffer.putUInt32(EnumWithValue.EnumUtils.toLong(completionFilter))
        // Reserved (4 bytes)
        buffer.putReserved4()
    }
}

enum class SMB2ChangeNotifyFlags(private val value: Long) : EnumWithValue<SMB2ChangeNotifyFlags> {
    SMB2_WATCH_TREE(0x0001);

    override fun getValue(): Long = value
}

// @see com.hierynomus.smbj.share.Share.send
@Throws(SMBRuntimeException::class)
private fun <T : SMB2Packet> Share.send(request: SMB2Packet): Future<T> {
    if (!isConnected) {
        throw SMBRuntimeException(javaClass.simpleName + " has already been closed")
    }
    val session = treeConnect.session
    return try {
        session.send(request)
    } catch (e: TransportException) {
        throw SMBRuntimeException(e)
    }
}
