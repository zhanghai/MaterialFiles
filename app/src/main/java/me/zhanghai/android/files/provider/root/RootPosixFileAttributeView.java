/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;
import me.zhanghai.android.files.provider.remote.RemotePosixFileAttributeView;

public class RootPosixFileAttributeView<FA extends PosixFileAttributes>
        extends RemotePosixFileAttributeView<FA> {

    public RootPosixFileAttributeView(@NonNull PosixFileAttributeView attributeView) {
        super(new RemoteInterfaceHolder<>(() -> RootFileService.getInstance()
                .getRemotePosixFileAttributeViewInterface(attributeView)));
    }

    @Override
    public String name() {
        throw new AssertionError();
    }
}
