/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import android.os.Parcelable
import java8.nio.file.Path
import java8.nio.file.attribute.FileAttributeView
import me.zhanghai.android.files.provider.common.PosixFileStore
import java.io.IOException

abstract class RootablePosixFileStore(
    private val path: Path,
    private val localFileStore: PosixFileStore,
    rootFileStoreCreator: (PosixFileStore) -> RootPosixFileStore
) : PosixFileStore(), Parcelable {
    private val rootFileStore: RootPosixFileStore = rootFileStoreCreator(this)

    @Throws(IOException::class)
    override fun refresh() {
        localFileStore.refresh()
    }

    override fun name(): String = localFileStore.name()

    override fun type(): String = localFileStore.type()

    override fun isReadOnly(): Boolean = localFileStore.isReadOnly

    @Throws(IOException::class)
    override fun setReadOnly(readOnly: Boolean) {
        callRootable(path) {
            isReadOnly = readOnly
            if (this == rootFileStore) {
                localFileStore.refresh()
            }
        }
    }

    @Throws(IOException::class)
    override fun getTotalSpace(): Long = callRootable(path) { totalSpace }

    @Throws(IOException::class)
    override fun getUsableSpace(): Long = callRootable(path) { usableSpace }

    @Throws(IOException::class)
    override fun getUnallocatedSpace(): Long = callRootable(path) { unallocatedSpace }

    override fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        localFileStore.supportsFileAttributeView(type)

    override fun supportsFileAttributeView(name: String): Boolean =
        localFileStore.supportsFileAttributeView(name)

    @Throws(IOException::class)
    private fun <R> callRootable(path: Path, block: PosixFileStore.() -> R): R =
        callRootable(path, true, localFileStore, rootFileStore, block)
}
