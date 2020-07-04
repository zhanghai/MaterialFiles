/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.hierynomus.smbj.share;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.messages.SMB2ReadResponse;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class FileAccessor {
    /**
     * @see File#read(byte[], long, int, int)
     */
    public static byte[] read(File file, long offset, int length) throws SMBRuntimeException {
        SMB2ReadResponse response = file.share.read(file.fileId, offset, length);
        if (response.getHeader().getStatusCode() == NtStatus.STATUS_END_OF_FILE.getValue()) {
            return null;
        } else {
            return response.getData();
        }
    }
}
