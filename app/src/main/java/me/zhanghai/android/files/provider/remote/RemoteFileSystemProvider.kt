/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileStore
import java8.nio.file.LinkOption
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.spi.FileSystemProvider
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.getArgs
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class RemoteFileSystemProvider(
    private val remoteInterface: RemoteInterface<IRemoteFileSystemProvider>
) : FileSystemProvider(), PathObservableProvider, Searchable {
    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream =
        remoteInterface.get().call { exception ->
            newInputStream(file.toParcelable(), options.toParcelable(), exception)
        }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        val options = when (options) {
            is Serializable -> options
            else -> options.toSet() as Serializable
        }
        return remoteInterface.get().call { exception ->
            newByteChannel(
                file.toParcelable(), options.toParcelable(), attributes.toParcelable(), exception
            )
        }
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        val filter = when (filter) {
            is Parcelable -> filter
            filesAcceptAllFilter -> ParcelableAcceptAllFilter.instance
            else -> throw IllegalArgumentException("$filter is not Parcelable")
        }
        return remoteInterface.get().call { exception ->
            newDirectoryStream(directory.toParcelable(), filter.toParcelable(), exception)
        }.value
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        remoteInterface.get().call { exception ->
            createDirectory(directory.toParcelable(), attributes.toParcelable(), exception)
        }
    }

    @Throws(IOException::class)
    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        remoteInterface.get().call { exception ->
            createSymbolicLink(
                link.toParcelable(), target.toParcelable(), attributes.toParcelable(), exception
            )
        }
    }

    @Throws(IOException::class)
    override fun createLink(link: Path, existing: Path) {
        remoteInterface.get().call { exception ->
            createLink(link.toParcelable(), existing.toParcelable(), exception)
        }
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        remoteInterface.get().call { exception -> delete(path.toParcelable(), exception) }
    }

    @Throws(IOException::class)
    override fun readSymbolicLink(link: Path): Path =
        remoteInterface.get().call { exception ->
            readSymbolicLink(link.toParcelable(), exception)
        }.value()

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        var interruptible: RemoteCallback? = null
        try {
            runBlocking<Unit> {
                suspendCoroutine { continuation ->
                    val callback = RemoteCallback {
                        val exception = it.getArgs<CallbackArgs>().exception.value
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                    interruptible = remoteInterface.get().call {
                        copy(
                            source.toParcelable(), target.toParcelable(), options.toParcelable(),
                            callback
                        )
                    }
                }
            }
        } catch (e: InterruptedException) {
            interruptible?.sendResult(Bundle())
            throw InterruptedIOException().apply { initCause(e) }
        }
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        var interruptible: RemoteCallback? = null
        try {
            runBlocking<Unit> {
                suspendCoroutine { continuation ->
                    val callback = RemoteCallback {
                        val exception = it.getArgs<CallbackArgs>().exception.value
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                    interruptible = remoteInterface.get().call {
                        move(
                            source.toParcelable(), target.toParcelable(), options.toParcelable(),
                            callback
                        )
                    }
                }
            }
        } catch (e: InterruptedException) {
            interruptible?.sendResult(Bundle())
            throw InterruptedIOException().apply { initCause(e) }
        }
    }

    @Throws(IOException::class)
    override fun isSameFile(path: Path, path2: Path): Boolean =
        remoteInterface.get().call { exception ->
            isSameFile(path.toParcelable(), path2.toParcelable(), exception)
        }

    @Throws(IOException::class)
    override fun isHidden(path: Path): Boolean =
        remoteInterface.get().call { exception -> isHidden(path.toParcelable(), exception) }

    @Throws(IOException::class)
    override fun getFileStore(path: Path): FileStore =
        remoteInterface.get().call {
            exception -> getFileStore(path.toParcelable(), exception)
        }.value()

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        remoteInterface.get().call { exception ->
            checkAccess(path.toParcelable(), modes.toParcelable(), exception)
        }
    }

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A =
        remoteInterface.get().call { exception ->
            readAttributes(
                path.toParcelable(), type.toParcelable(), options.toParcelable(), exception
            )
        }.value()

    @Throws(IOException::class)
    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable =
        remoteInterface.get().call { exception ->
            observe(path.toParcelable(), intervalMillis, exception)
        }.also { it.initializeForRemote() }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        var interruptible: RemoteCallback? = null
        try {
            runBlocking<Unit> {
                suspendCoroutine { continuation ->
                    val callback = RemoteCallback {
                        val exception = it.getArgs<CallbackArgs>().exception.value
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                    interruptible = remoteInterface.get().call {
                        search(
                            directory.toParcelable(), query, intervalMillis,
                            listener.toParcelable(), callback
                        )
                    }
                }
            }
        } catch (e: InterruptedException) {
            interruptible?.sendResult(Bundle())
            throw InterruptedIOException().apply { initCause(e) }
        }
    }

    private class ParcelableAcceptAllFilter private constructor() : DirectoryStream.Filter<Path>,
        Parcelable {
        override fun accept(entry: Path): Boolean = true

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {}

        companion object {
            val instance = ParcelableAcceptAllFilter()

            @JvmField
            val CREATOR = object : Parcelable.Creator<ParcelableAcceptAllFilter> {
                override fun createFromParcel(source: Parcel): ParcelableAcceptAllFilter = instance

                override fun newArray(size: Int): Array<ParcelableAcceptAllFilter?> =
                    arrayOfNulls(size)
            }
        }
    }

    @Parcelize
    internal class CallbackArgs(val exception: ParcelableException) : ParcelableArgs
}
