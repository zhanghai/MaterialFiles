/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;

public class RemoteFileSystemInterface extends IRemoteFileSystem.Stub {

    @NonNull
    private final FileSystem mFileSystem;

    public RemoteFileSystemInterface(@NonNull FileSystem fileSystem) {
        mFileSystem = fileSystem;
    }

    @Override
    public void close(@NonNull ParcelableIoException ioException) {
        try {
            mFileSystem.close();
        } catch (IOException e) {
            ioException.set(e);
        }
    }
}
