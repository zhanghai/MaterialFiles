/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package net.schmizz.sshj.sftp;

import net.schmizz.concurrent.Promise;

import java.io.IOException;

import androidx.annotation.NonNull;

public class RemoteFileAccessor {
    private RemoteFileAccessor() {}

    @NonNull
    public static Promise<Response, SFTPException> asyncRead(@NonNull RemoteFile file, long offset,
                                                             int length) throws IOException {
        return file.asyncRead(offset, length);
    }

    @NonNull
    public static SFTPEngine getRequester(@NonNull RemoteFile file) {
        return file.requester;
    }
}
