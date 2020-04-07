/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content.resolver

import android.database.Cursor

@Throws(ResolverException::class)
fun Cursor.moveToFirstOrThrow() {
    if (!moveToFirst()) {
        throw ResolverException("Cursor.moveToFirst() returned false")
    }
}

@Throws(ResolverException::class)
fun Cursor.getInt(columnName: String): Int {
    val columnIndex = try {
        getColumnIndexOrThrow(columnName)
    } catch (e: IllegalArgumentException) {
        throw ResolverException(e)
    }
    return getInt(columnIndex)
}

@Throws(ResolverException::class)
fun Cursor.getLong(columnName: String): Long {
    val columnIndex = try {
        getColumnIndexOrThrow(columnName)
    } catch (e: IllegalArgumentException) {
        throw ResolverException(e)
    }
    return getLong(columnIndex)
}

@Throws(ResolverException::class)
fun Cursor.getString(columnName: String): String? {
    val columnIndex = try {
        getColumnIndexOrThrow(columnName)
    } catch (e: IllegalArgumentException) {
        throw ResolverException(e)
    }
    return getString(columnIndex)
}

@Throws(ResolverException::class)
fun Cursor.requireString(columnName: String): String {
    return getString(columnName)
        ?: throw ResolverException("Cursor.getString() with column name $columnName returned null")
}
