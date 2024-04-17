package me.zhanghai.android.files.compat

import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import java.util.Calendar
import java.util.Date

fun Calendar.toInstantCompat(): Instant = DateTimeUtils.toInstant(this)

fun Date.toInstantCompat(): Instant = DateTimeUtils.toInstant(this)

fun Instant.toDateCompat(): Date = DateTimeUtils.toDate(this)
