/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.text.format.DateUtils
import org.threeten.bp.Duration

fun Duration.format(): String = DateUtils.formatElapsedTime(seconds)
