/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 *
 * Based on com.hierynomus.smbj.auth.NtlmAuthenticator from SMBJ 0.11.5,
 * Copyright (C) 2016 - SMBJ Contributors, licensed under the Apache License, Version 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.asn1.types.primitive.ASN1ObjectIdentifier
import com.hierynomus.ntlm.functions.NtlmFunctions
import com.hierynomus.ntlm.messages.AvId
import com.hierynomus.ntlm.messages.NtlmAuthenticate
import com.hierynomus.ntlm.messages.NtlmChallenge
import com.hierynomus.ntlm.messages.NtlmNegotiate
import com.hierynomus.ntlm.messages.NtlmNegotiateFlag
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.protocol.commons.Factory as SmbjFactory
import com.hierynomus.protocol.commons.buffer.Buffer
import com.hierynomus.protocol.commons.buffer.Endian
import com.hierynomus.security.SecurityProvider
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticateResponse
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.auth.Authenticator
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.connection.ConnectionContext
import com.hierynomus.spnego.NegTokenInit
import com.hierynomus.spnego.NegTokenTarg
import com.hierynomus.spnego.SpnegoException
import java.io.IOException
import java.util.Random

/**
 * A copy of SMBJ 0.11.5's `NtlmAuthenticator` with a correctly computed MIC.
 *
 * SMBJ 0.11.5 includes a MIC whenever the server sends `MsvAvTimestamp`, but computes it as
 * `HMAC_MD5(SessionBaseKey, CHALLENGE || ServerChallenge || AUTHENTICATE header)`. [MS-NLMP]
 * 3.1.5.1.2 requires `HMAC_MD5(ExportedSessionKey, NEGOTIATE || CHALLENGE || AUTHENTICATE)` with
 * the MIC field zeroed. Windows only verifies the MIC when the client sets `MsvAvFlags` bit 0x2
 * (which SMBJ doesn't), so it tolerates the bad value; macOS's Heimdal NTLM acceptor verifies any
 * MIC that is present, and fails the session setup with `HNTLM_ERR_INVALID_MIC`, surfacing as
 * `STATUS_LOGON_FAILURE`.
 *
 * @see https://github.com/hierynomus/smbj/issues/698
 */
class MicFixedNtlmAuthenticator : Authenticator {
    private lateinit var securityProvider: SecurityProvider
    private lateinit var random: Random
    private var workStationName: String? = null

    private var negotiateMessage: ByteArray? = null
    private var completed = false

    @Throws(IOException::class)
    override fun authenticate(
        context: AuthenticationContext,
        gssToken: ByteArray?,
        connectionContext: ConnectionContext?
    ): AuthenticateResponse? =
        try {
            when {
                completed -> null
                negotiateMessage == null -> createNegotiateResponse()
                else -> createAuthenticateResponse(context, gssToken!!)
            }
        } catch (e: SpnegoException) {
            throw SMBRuntimeException(e)
        }

    private fun createNegotiateResponse(): AuthenticateResponse {
        val negotiateMessage = Buffer.PlainBuffer(Endian.LE)
            .also { NtlmNegotiate().write(it) }
            .compactData
        this.negotiateMessage = negotiateMessage
        val negTokenInit = NegTokenInit().apply {
            addSupportedMech(NTLMSSP)
            setMechToken(negotiateMessage)
        }
        return AuthenticateResponse().apply {
            negToken = Buffer.PlainBuffer(Endian.LE).also { negTokenInit.write(it) }.compactData
        }
    }

    private fun createAuthenticateResponse(
        context: AuthenticationContext,
        gssToken: ByteArray
    ): AuthenticateResponse {
        val functions = NtlmFunctions(random, securityProvider)
        val challengeMessage = NegTokenTarg().read(gssToken).responseToken
        val challenge = NtlmChallenge()
        try {
            challenge.read(Buffer.PlainBuffer(challengeMessage, Endian.LE))
        } catch (e: Buffer.BufferException) {
            throw IOException(e)
        }
        val response = AuthenticateResponse()
        response.windowsVersion = challenge.version
        response.netBiosName = challenge.targetInfo.getAvPairString(AvId.MsvAvNbComputerName)

        val responseKeyNt =
            functions.NTOWFv2(String(context.password), context.username, context.domain)
        val clientTargetInfo = challenge.targetInfo.copy()
        val flags = challenge.negotiateFlags
        if (NtlmNegotiateFlag.NTLMSSP_REQUEST_TARGET in flags) {
            clientTargetInfo.putAvPairString(
                AvId.MsvAvTargetName,
                "cifs/${clientTargetInfo.getAvPairString(AvId.MsvAvDnsComputerName)}"
            )
        }
        val ntlmV2ClientChallenge = functions.getNTLMv2ClientChallenge(clientTargetInfo)
        val ntlmV2Response = functions.getNTLMv2Response(
            responseKeyNt, challenge.serverChallenge, ntlmV2ClientChallenge
        )
        // The first 16 bytes of the NTLMv2 response is NTProofStr.
        val sessionBaseKey = functions.hmac_md5(responseKeyNt, ntlmV2Response.copyOf(16))
        val exportedSessionKey: ByteArray
        val encryptedRandomSessionKey: ByteArray
        if (NtlmNegotiateFlag.NTLMSSP_NEGOTIATE_KEY_EXCH in flags && (
                NtlmNegotiateFlag.NTLMSSP_NEGOTIATE_SIGN in flags
                    || NtlmNegotiateFlag.NTLMSSP_NEGOTIATE_SEAL in flags
                    || NtlmNegotiateFlag.NTLMSSP_NEGOTIATE_ALWAYS_SIGN in flags)) {
            exportedSessionKey = ByteArray(16).also { random.nextBytes(it) }
            encryptedRandomSessionKey = functions.encryptRc4(sessionBaseKey, exportedSessionKey)
        } else {
            exportedSessionKey = sessionBaseKey
            encryptedRandomSessionKey = sessionBaseKey
        }
        response.sessionKey = exportedSessionKey
        completed = true

        val useMic = challenge.targetInfo.getAvPairObject(AvId.MsvAvTimestamp) != null
        var negotiateFlags = EnumWithValue.EnumUtils.toLong(flags)
        if (useMic) {
            // The MIC is at offset 72, right after the 8-byte Version field, so the Version field
            // must be present for servers to locate it.
            negotiateFlags = negotiateFlags or NtlmNegotiateFlag.NTLMSSP_NEGOTIATE_VERSION.value
        }
        val authenticate = NtlmAuthenticate(
            ByteArray(0), ntlmV2Response, context.username, context.domain, workStationName,
            encryptedRandomSessionKey, negotiateFlags, useMic
        )
        if (useMic) {
            authenticate.setMic(ByteArray(16))
            val authenticateMessageWithZeroMic =
                Buffer.PlainBuffer(Endian.LE).also { authenticate.write(it) }.compactData
            authenticate.setMic(
                functions.hmac_md5(
                    exportedSessionKey, negotiateMessage!!, challengeMessage,
                    authenticateMessageWithZeroMic
                )
            )
        }
        val negTokenTarg = NegTokenTarg().apply {
            responseToken = Buffer.PlainBuffer(Endian.LE).also { authenticate.write(it) }
                .compactData
        }
        response.negToken = Buffer.PlainBuffer(Endian.LE).also { negTokenTarg.write(it) }
            .compactData
        return response
    }

    override fun init(config: SmbConfig) {
        securityProvider = config.securityProvider
        random = config.randomProvider
        workStationName = config.workStationName
    }

    override fun supports(context: AuthenticationContext): Boolean =
        context.javaClass == AuthenticationContext::class.java

    class Factory : SmbjFactory.Named<Authenticator> {
        override fun getName(): String = NTLMSSP.value

        override fun create(): Authenticator = MicFixedNtlmAuthenticator()
    }

    companion object {
        private val NTLMSSP = ASN1ObjectIdentifier("1.3.6.1.4.1.311.2.2.10")

        /**
         * Returns SMBJ's default authenticators with the NTLM one replaced by
         * [MicFixedNtlmAuthenticator].
         */
        fun replaceInDefaultAuthenticators(): List<SmbjFactory.Named<Authenticator>> =
            SmbConfig.createDefaultConfig().supportedAuthenticators.map {
                if (it.name == NTLMSSP.value) Factory() else it
            }
    }
}
