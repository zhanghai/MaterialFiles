/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.content.Intent
import java8.nio.file.FileSystemException

abstract class UserActionRequiredException : FileSystemException {
    constructor(file: String?) : super(file)

    constructor(file: String?, other: String?, reason: String?) : super(file, other, reason)

    abstract fun getUserAction(): Intent
}
