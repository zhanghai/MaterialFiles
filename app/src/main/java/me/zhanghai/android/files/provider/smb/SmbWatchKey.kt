/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import me.zhanghai.android.files.provider.common.AbstractWatchKey

internal class SmbWatchKey(
    watchService: SmbWatchService,
    path: SmbPath
) : AbstractWatchKey(watchService, path) {
    override val watchService: SmbWatchService
        get() = super.watchService as SmbWatchService

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

    override fun watchable(): SmbPath = super.watchable() as SmbPath
}
