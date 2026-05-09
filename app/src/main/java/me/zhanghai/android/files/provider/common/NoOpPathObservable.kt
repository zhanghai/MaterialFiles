/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

class NoOpPathObservable : PathObservable {
    override fun addObserver(observer: () -> Unit) {}
    override fun removeObserver(observer: () -> Unit) {}
    override fun close() {}
}
