/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.content.Intent
import me.zhanghai.android.files.provider.common.UserActionRequiredException

class ArchivePasswordRequiredException : UserActionRequiredException {
    constructor(file: String?) : super(file)

    constructor(file: String?, other: String?, reason: String?) : super(file, other, reason)

    override fun getUserAction(): Intent = TODO()
}
