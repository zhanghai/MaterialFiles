/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import me.zhanghai.android.files.app.AppProvider

fun <T : Parcelable> Bundle.getParcelableSafe(key: String): T? {
    classLoader = AppProvider::class.java.classLoader
    return getParcelable(key)
}

fun Bundle.getParcelableArraySafe(key: String): Array<Parcelable>? {
    classLoader = AppProvider::class.java.classLoader
    return getParcelableArray(key)
}

fun <T : Parcelable?> Bundle.getParcelableArrayListSafe(key: String): ArrayList<T>? {
    classLoader = AppProvider::class.java.classLoader
    return getParcelableArrayList(key)
}

fun <T : Parcelable?> Bundle.getSparseParcelableArraySafe(key: String): SparseArray<T>? {
    classLoader = AppProvider::class.java.classLoader
    return getSparseParcelableArray(key)
}
