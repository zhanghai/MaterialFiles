/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.hierynomus.smbj.share;

import androidx.annotation.NonNull;

import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.messages.SMB2IoctlResponse;
import com.hierynomus.smbj.io.ArrayByteChunkProvider;
import com.hierynomus.smbj.io.ByteChunkProvider;

import java.util.concurrent.Future;

public class ShareAccessor {
    private ShareAccessor() {}

    /**
     * This ioctl() variant allows passing in the {@param statusHandler}.
     *
     * @see Share#ioctl(com.hierynomus.mssmb2.SMB2FileId, long, boolean, ByteChunkProvider, int)
     */
    @NonNull
    public static SMB2IoctlResponse ioctl(@NonNull Share share, @NonNull SMB2FileId fileId,
                                          long ctlCode, boolean isFsCtl, @NonNull byte[] inData,
                                          int inOffset, int inLength,
                                          @NonNull StatusHandler statusHandler, long timeout) {
        final ByteChunkProvider inputData = new ArrayByteChunkProvider(inData, inOffset, inLength,
                0);
        final Future<SMB2IoctlResponse> future = share.ioctlAsync(fileId, ctlCode, isFsCtl,
                inputData, -1);
        return share.receive(future, "IOCTL", fileId, statusHandler, timeout);
    }
}
