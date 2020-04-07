/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import java8.nio.file.attribute.FileAttributeView
import me.zhanghai.android.files.provider.common.PosixFileStore
import java.io.IOException

abstract class RemotePosixFileStore(
    private val remoteInterface: RemoteInterface<IRemotePosixFileStore>
) : PosixFileStore() {
    override fun refresh() {
        throw AssertionError()
    }

    override fun name(): String {
        throw AssertionError()
    }

    override fun type(): String {
        throw AssertionError()
    }

    override fun isReadOnly(): Boolean {
        throw AssertionError()
    }

    @Throws(IOException::class)
    override fun setReadOnly(readOnly: Boolean) {
        remoteInterface.get().call { exception -> setReadOnly(readOnly, exception) }
    }

    @Throws(IOException::class)
    override fun getTotalSpace(): Long =
        remoteInterface.get().call { exception -> getTotalSpace(exception) }

    @Throws(IOException::class)
    override fun getUsableSpace(): Long =
        remoteInterface.get().call { exception -> getUsableSpace(exception) }

    @Throws(IOException::class)
    override fun getUnallocatedSpace(): Long =
        remoteInterface.get().call { exception -> getUnallocatedSpace(exception) }

    override fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean {
        throw AssertionError()
    }

    override fun supportsFileAttributeView(name: String): Boolean {
        throw AssertionError()
    }
}
