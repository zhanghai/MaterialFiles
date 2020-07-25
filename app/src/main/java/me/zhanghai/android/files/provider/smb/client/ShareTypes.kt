/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-srvs/6069f8c0-c93f-43a0-a5b4-7ed447eb4b84
enum class ShareTypes(val value: Int) {
    STYPE_DISKTREE(0x00000000),
    STYPE_PRINTQ(0x00000001),
    STYPE_DEVICE(0x00000002),
    STYPE_IPC(0x00000003),
    STYPE_CLUSTER_FS(0x02000000),
    STYPE_CLUSTER_SOFS(0x04000000),
    STYPE_CLUSTER_DFS(0x08000000),
    STYPE_SPECIAL(0x80000000.toInt()),
    STYPE_TEMPORARY(0x40000000)
}
