/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

enum class FileJobConflictAction {
    MERGE_OR_REPLACE,
    RENAME,
    SKIP,
    CANCEL,
    CANCELED
}
