/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import androidx.annotation.NonNull;

public interface RemotableFileStore {

    @NonNull
    RemoteFileStore toRemote();
}
