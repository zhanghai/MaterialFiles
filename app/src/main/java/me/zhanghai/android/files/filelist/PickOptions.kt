/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import me.zhanghai.android.files.file.MimeType

class PickOptions(
    val readOnly: Boolean,
    val pickDirectory: Boolean,
    val mimeTypes: List<MimeType>,
    val localOnly: Boolean,
    val allowMultiple: Boolean
)
