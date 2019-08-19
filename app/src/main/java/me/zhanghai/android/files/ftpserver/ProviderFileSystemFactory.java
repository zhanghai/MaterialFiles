/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.User;

import androidx.annotation.NonNull;

class ProviderFileSystemFactory implements FileSystemFactory {

    @NonNull
    @Override
    public FileSystemView createFileSystemView(@NonNull User user) {
        return new ProviderFileSystemView(user);
    }
}
