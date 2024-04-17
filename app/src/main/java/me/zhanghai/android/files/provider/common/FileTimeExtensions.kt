/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.attribute.FileTime
import org.threeten.bp.Instant
import kotlin.reflect.KClass

val KClass<FileTime>.EPOCH: FileTime
    get() = FileTime.from(Instant.EPOCH)
