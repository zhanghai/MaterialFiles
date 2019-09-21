/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;
import me.zhanghai.android.files.provider.remote.RemotePosixFileStore;

public class RootPosixFileStore extends RemotePosixFileStore {

    public RootPosixFileStore(@NonNull PosixFileStore fileStore) {
        super(new RemoteInterfaceHolder<>(() -> RootFileService.getInstance()
                .getRemotePosixFileStoreInterface(fileStore)));
    }
}
