/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content.resolver

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

@Throws(ResolverException::class)
fun Cursor.moveToFirstOrThrow() {
    if (!moveToFirst()) {
        throw ResolverException("Cursor.moveToFirst() returned false")
    }
}

fun Cursor.getColumnIndexOrNull(columnName: String): Int? =
    getColumnIndex(columnName).takeIf { it != -1 }

fun Cursor.getInt(columnName: String): Int? {
    val columnIndex = getColumnIndexOrNull(columnName) ?: return null
    return getIntOrNull(columnIndex)
}

fun Cursor.getLong(columnName: String): Long? {
    val columnIndex = getColumnIndexOrNull(columnName) ?: return null
    return getLongOrNull(columnIndex)
}

fun Cursor.getString(columnName: String): String? {
    val columnIndex = getColumnIndexOrNull(columnName) ?: return null
    return getStringOrNull(columnIndex)
}

@Throws(ResolverException::class)
fun Cursor.requireColumnIndex(columnName: String): Int =
    try {
        getColumnIndexOrThrow(columnName)
    } catch (e: IllegalArgumentException) {
        throw ResolverException(e)
    }

@Throws(ResolverException::class)
fun Cursor.requireString(columnName: String): String {
    return getStringOrNull(requireColumnIndex(columnName))
        ?: throw ResolverException("Cursor.getStringOrNull() for $columnName returned null")
}
