/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.hierynomus.smbj.share;

import com.hierynomus.mssmb2.messages.SMB2ReadResponse;
import com.hierynomus.smbj.common.SMBRuntimeException;

import java.util.concurrent.Future;

import androidx.annotation.NonNull;

public class FileAccessor {
    private FileAccessor() {}

    /**
     * @see File#readAsync(long, int)
     */
    @NonNull
    public static Future<SMB2ReadResponse> readAsync(@NonNull File file, long offset, int length)
            throws SMBRuntimeException {
        return file.readAsync(offset, length);
    }
}
