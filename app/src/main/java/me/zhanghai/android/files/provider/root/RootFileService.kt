/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import android.os.Process
import me.zhanghai.android.files.provider.remote.RemoteFileService
import me.zhanghai.android.files.provider.remote.RemoteInterface

val isRunningAsRoot = Process.myUid() == 0

object RootFileService : RemoteFileService(
    RemoteInterface {
        if (SuiFileServiceLauncher.isSuiAvailable) {
            SuiFileServiceLauncher.launchService()
        } else {
            LibRootJavaFileServiceLauncher.launchService()
        }
    }
)
