/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.compat.checkSelfPermissionCompat
import me.zhanghai.android.files.compat.readBooleanCompat
import me.zhanghai.android.files.compat.writeBooleanCompat
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.provider.root.RootablePath
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

internal class LinuxPath : ByteStringListPath<LinuxPath>, RootablePath {
    private val fileSystem: LinuxFileSystem

    constructor(fileSystem: LinuxFileSystem, path: ByteString) : super(
        LinuxFileSystem.SEPARATOR, path
    ) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: LinuxFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(LinuxFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == LinuxFileSystem.SEPARATOR

    override fun createPath(path: ByteString): LinuxPath = LinuxPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): LinuxPath =
        LinuxPath(fileSystem, absolute, segments)

    override val uriSchemeSpecificPart: ByteString?
        get() =
            ByteStringBuilder(BYTE_STRING_TWO_SLASHES)
                .append(super.uriSchemeSpecificPart!!)
                .toByteString()

    override val defaultDirectory: LinuxPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): LinuxFileSystem = fileSystem

    override fun getRoot(): LinuxPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): LinuxPath {
        throw UnsupportedOperationException()
    }

    override fun toFile(): File = File(toString())

    @Throws(IOException::class)
    override fun register(
        watcher: WatchService,
        events: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey {
        if (watcher !is LocalLinuxWatchService) {
            throw ProviderMismatchException(watcher.toString())
        }
        return watcher.register(this, events, *modifiers)
    }

    @Volatile
    override var isRootPreferred: Boolean = false

    override val rootStrategy: RootStrategy
        get() {
            val strategy = super.rootStrategy
            if (strategy == RootStrategy.PREFER_NO && isMissingStoragePermission) {
                return RootStrategy.NEVER
            }
            return strategy
        }

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
        isRootPreferred = source.readBooleanCompat()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
        dest.writeBooleanCompat(isRootPreferred)
    }

    companion object {
        private val BYTE_STRING_TWO_SLASHES = "//".toByteString()

        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxPath> {
            override fun createFromParcel(source: Parcel): LinuxPath = LinuxPath(source)

            override fun newArray(size: Int): Array<LinuxPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isLinuxPath: Boolean
    get() = this is LinuxPath

// IPC for checking the storage permission is expensive if we do it on every file system access, so
// cache its result here.
@Volatile
private var wasMissingStoragePermission = true
private val isMissingStoragePermission: Boolean
    get() {
        // Android kills an app if the user revokes any of its runtime permissions.
        if (!wasMissingStoragePermission) {
            return false
        }
        return (Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.R
                && application.checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            .also { wasMissingStoragePermission = it }
    }
