/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mssmb2.SMB2MessageCommandCode
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.protocol.commons.buffer.Buffer
import com.hierynomus.protocol.commons.buffer.Endian
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.share.DiskEntry
import me.zhanghai.android.files.util.hasBits

// @see https://github.com/torvalds/linux/blob/master/fs/cifs/smbfsctl.h
private const val FSCTL_SET_REPARSE_POINT = 0x000900A4
private const val FSCTL_GET_REPARSE_POINT = 0x000900A8

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/c8e77b37-3909-4fe6-a4ea-2b9d423b1ee4
private const val IO_REPARSE_TAG_SYMLINK = 0xA000000CL

private const val PATH_BUFFER_OFFSET = 20

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/b41f1cbf-10df-4a47-98d4-1c52a833d913
private const val SYMLINK_FLAG_RELATIVE = 0x00000001L

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/b41f1cbf-10df-4a47-98d4-1c52a833d913
class SymbolicLinkReparseData(
    val substituteName: String,
    val printName: String,
    val isRelative: Boolean
)

// @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/cli_smb2_fnum.c
//      cli_smb2_get_reparse_point_fnum_send
// @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/reparse_symlink.c
//      symlink_reparse_buffer_parse
// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/d86a4c4d-a996-403a-8b92-9c0e1a300e22
// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/b41f1cbf-10df-4a47-98d4-1c52a833d913
@Throws(SMBRuntimeException::class)
internal fun DiskEntry.getSymbolicLinkReparseData(): SymbolicLinkReparseData {
    val buffer = Buffer.PlainBuffer(
        ioctl(FSCTL_GET_REPARSE_POINT, true, ByteArray(0), 0, 0), Endian.LE
    )
    return try {
        // ReparseTag (4 bytes)
        val reparseTag = buffer.readUInt32()
        if (reparseTag != IO_REPARSE_TAG_SYMLINK) {
            throw SMBApiException(
                NtStatuses.STATUS_IO_REPARSE_TAG_MISMATCH, SMB2MessageCommandCode.SMB2_IOCTL, null
            )
        }
        // ReparseDataLength (2 bytes)
        buffer.readUInt16()
        // Reserved (2 bytes)
        buffer.readUInt16()
        // SubstituteNameOffset (2 bytes)
        val substituteNameOffset = buffer.readUInt16()
        // SubstituteNameLength (2 bytes)
        val substituteNameLength = buffer.readUInt16()
        // PrintNameOffset (2 bytes)
        val printNameOffset = buffer.readUInt16()
        // PrintNameLength (2 bytes)
        val printNameLength = buffer.readUInt16()
        // Flags (4 bytes)
        val flags = buffer.readUInt32()
        buffer.rpos(PATH_BUFFER_OFFSET + substituteNameOffset)
        val substituteName = buffer.readString(Charsets.UTF_16LE, substituteNameLength / 2)
        buffer.rpos(PATH_BUFFER_OFFSET + printNameOffset)
        val printName = buffer.readString(Charsets.UTF_16LE, printNameLength / 2)
        val isRelative = flags.hasBits(SYMLINK_FLAG_RELATIVE)
        SymbolicLinkReparseData(substituteName, printName, isRelative)
    } catch (e: Buffer.BufferException) {
        throw SMBRuntimeException(e)
    }
}

// @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/cli_smb2_fnum.c
//      cli_smb2_set_reparse_point_fnum_send
// @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/reparse_symlink.c
//      symlink_reparse_buffer_marshall
// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/4dc2b168-f177-4eec-a14b-25a51cbba2cf
// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-fscc/b41f1cbf-10df-4a47-98d4-1c52a833d913
@Throws(SMBRuntimeException::class)
internal fun DiskEntry.setSymbolicLinkReparseData(reparseData: SymbolicLinkReparseData) {
    val substituteNameLength = reparseData.substituteName.length * 2
    val printNameLength = reparseData.printName.length * 2
    val reparseDataLength = 12 + substituteNameLength + printNameLength
    val buffer = Buffer.PlainBuffer(Endian.LE)
    // ReparseTag (4 bytes)
    buffer.putUInt32(IO_REPARSE_TAG_SYMLINK)
    // ReparseDataLength (2 bytes)
    buffer.putUInt16(reparseDataLength)
    // Reserved (2 bytes)
    buffer.putUInt16(0)
    // SubstituteNameOffset (2 bytes)
    buffer.putUInt16(0)
    // SubstituteNameLength (2 bytes)
    buffer.putUInt16(substituteNameLength)
    // PrintNameOffset (2 bytes)
    buffer.putUInt16(substituteNameLength)
    // PrintNameLength (2 bytes)
    buffer.putUInt16(printNameLength)
    // Flags (4 bytes)
    buffer.putUInt32(if (reparseData.isRelative) SYMLINK_FLAG_RELATIVE else 0)
    // PathBuffer (variable)
    buffer.putString(reparseData.substituteName, Charsets.UTF_16LE)
    buffer.putString(reparseData.printName, Charsets.UTF_16LE)
    ioctl(FSCTL_SET_REPARSE_POINT, true, buffer.array(), buffer.rpos(), buffer.available())
}
