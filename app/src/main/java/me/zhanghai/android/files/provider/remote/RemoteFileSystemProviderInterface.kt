/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.toBundle
import java.util.concurrent.Executors

class RemoteFileSystemProviderInterface(
    private val provider: FileSystemProvider
) : IRemoteFileSystemProvider.Stub() {
    private val executorService = Executors.newCachedThreadPool()

    override fun newInputStream(
        file: ParcelableObject,
        options: ParcelableSerializable,
        exception: ParcelableException
    ): RemoteInputStream? =
        tryRun(exception) { provider.newInputStream(file.value(), *options.value()).toRemote() }

    override fun newByteChannel(
        file: ParcelableObject,
        options: ParcelableSerializable,
        attributes: ParcelableFileAttributes,
        exception: ParcelableException
    ): RemoteSeekableByteChannel? =
        tryRun(exception) {
            provider.newByteChannel(file.value(), options.value(), *attributes.value).toRemote()
        }

    override fun newDirectoryStream(
        directory: ParcelableObject,
        filter: ParcelableObject,
        exception: ParcelableException
    ): ParcelableDirectoryStream? =
        tryRun(exception) {
            provider.newDirectoryStream(directory.value(), filter.value())
                .use { ParcelableDirectoryStream(it) }
        }

    override fun createDirectory(
        directory: ParcelableObject,
        attributes: ParcelableFileAttributes,
        exception: ParcelableException
    ) {
        tryRun(exception) { provider.createDirectory(directory.value(), *attributes.value) }
    }

    override fun createSymbolicLink(
        link: ParcelableObject,
        target: ParcelableObject,
        attributes: ParcelableFileAttributes,
        exception: ParcelableException
    ) {
        tryRun(exception) {
            provider.createSymbolicLink(link.value(), target.value(), *attributes.value)
        }
    }

    override fun createLink(
        link: ParcelableObject,
        existing: ParcelableObject,
        exception: ParcelableException
    ) {
        tryRun(exception) { provider.createLink(link.value(), existing.value()) }
    }

    override fun delete(path: ParcelableObject, exception: ParcelableException) {
        tryRun(exception) { provider.delete(path.value()) }
    }

    override fun readSymbolicLink(
        link: ParcelableObject,
        exception: ParcelableException
    ): ParcelableObject? =
        tryRun(exception) { provider.readSymbolicLink(link.value()).toParcelable() }

    override fun copy(
        source: ParcelableObject,
        target: ParcelableObject,
        options: ParcelableCopyOptions,
        callback: RemoteCallback
    ): RemoteCallback {
        val future = executorService.submit<Unit> {
            val exception = ParcelableException()
            tryRun(exception) {
                provider.copy(source.value(), target.value(), *options.value)
            }
            callback.sendResult(RemoteFileSystemProvider.CallbackArgs(exception).toBundle())
        }
        return RemoteCallback { future.cancel(true) }
    }

    override fun move(
        source: ParcelableObject,
        target: ParcelableObject,
        options: ParcelableCopyOptions,
        callback: RemoteCallback
    ): RemoteCallback {
        val future = executorService.submit<Unit> {
            val exception = ParcelableException()
            tryRun(exception) {
                provider.move(source.value(), target.value(), *options.value)
            }
            callback.sendResult(RemoteFileSystemProvider.CallbackArgs(exception).toBundle())
        }
        return RemoteCallback { future.cancel(true) }
    }

    override fun isSameFile(
        path: ParcelableObject,
        path2: ParcelableObject,
        exception: ParcelableException
    ): Boolean = tryRun(exception) { provider.isSameFile(path.value(), path2.value()) } ?: false

    override fun isHidden(path: ParcelableObject, exception: ParcelableException): Boolean =
        tryRun(exception) { provider.isHidden(path.value()) } ?: false

    override fun getFileStore(
        path: ParcelableObject,
        exception: ParcelableException
    ): ParcelableObject? = tryRun(exception) { provider.getFileStore(path.value()).toParcelable() }

    override fun checkAccess(
        path: ParcelableObject,
        modes: ParcelableSerializable,
        exception: ParcelableException
    ) {
        tryRun(exception) { provider.checkAccess(path.value(), *modes.value()) }
    }

    override fun readAttributes(
        path: ParcelableObject,
        type: ParcelableSerializable,
        options: ParcelableSerializable,
        exception: ParcelableException
    ): ParcelableObject? =
        tryRun(exception) {
            provider.readAttributes(
                // We have to explicitly specify the Class type here, or it will be resolved to the
                // String overload.
                path.value(), type.value<Class<BasicFileAttributes>>(), *options.value()
            ).toParcelable()
        }

    override fun observe(
        path: ParcelableObject,
        intervalMillis: Long,
        exception: ParcelableException
    ): RemotePathObservable? =
        tryRun(exception) {
            (provider as PathObservableProvider).observe(path.value(), intervalMillis).toRemote()
        }

    override fun search(
        directory: ParcelableObject,
        query: String,
        intervalMillis: Long,
        listener: ParcelablePathListConsumer,
        callback: RemoteCallback
    ): RemoteCallback {
        val future = executorService.submit<Unit> {
            val exception = ParcelableException()
            tryRun(exception) {
                (provider as Searchable).search(
                    directory.value(), query, intervalMillis, listener.value
                )
            }
            callback.sendResult(RemoteFileSystemProvider.CallbackArgs(exception).toBundle())
        }
        return RemoteCallback { future.cancel(true) }
    }
}
