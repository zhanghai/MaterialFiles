package me.zhanghai.android.files.compat

import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import java.util.Calendar

fun Calendar.toInstantCompat(): Instant = DateTimeUtils.toInstant(this)
