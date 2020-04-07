/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import me.zhanghai.android.files.provider.common.AbstractWatchKey

internal class LocalLinuxWatchKey(
    watchService: LocalLinuxWatchService,
    path: LinuxPath,
    val watchDescriptor: Int
) : AbstractWatchKey(watchService, path) {
    override val watchService: LocalLinuxWatchService
        get() = super.watchService as LocalLinuxWatchService

    private var isValid = true

    override fun isValid(): Boolean {
        synchronized(lock) { return isValid }
    }

    fun setInvalid() {
        synchronized(lock) { isValid = false }
    }

    override fun cancel() {
        synchronized(lock) {
            if (isValid) {
                watchService.cancel(this)
            }
        }
    }

    override fun watchable(): LinuxPath = super.watchable() as LinuxPath
}
