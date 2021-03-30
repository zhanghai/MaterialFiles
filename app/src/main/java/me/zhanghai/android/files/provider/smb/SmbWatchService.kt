/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileNotifyAction
import com.hierynomus.mssmb2.SMB2CompletionFilter
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.mssmb2.messages.SMB2ChangeNotifyResponse
import com.hierynomus.smbj.share.Directory
import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchEvent
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.provider.common.AbstractWatchService
import me.zhanghai.android.files.provider.smb.client.Client
import me.zhanghai.android.files.provider.smb.client.ClientException
import me.zhanghai.android.files.util.closeSafe
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

// @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/05869c32-39f0-4726-afc9-671b76ae5ca7
internal class SmbWatchService : AbstractWatchService<SmbWatchKey>() {
    private val notifiers = mutableMapOf<SmbPath, Notifier>()

    @Throws(IOException::class)
    fun register(
        path: SmbPath,
        kinds: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): SmbWatchKey {
        val kindSet = mutableSetOf<WatchEvent.Kind<*>>()
        for (kind in kinds) {
            when (kind) {
                StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY -> kindSet += kind
                // Ignored.
                StandardWatchEventKinds.OVERFLOW -> {}
                else -> throw UnsupportedOperationException(kind.name())
            }
        }
        for (modifier in modifiers) {
            throw UnsupportedOperationException(modifier.name())
        }
        synchronized(notifiers) {
            var notifier = notifiers[path]
            if (notifier != null) {
                notifier.kinds = kindSet
            } else {
                notifier = Notifier(this, path, kindSet)
                notifiers[path] = notifier
                notifier.start()
            }
            return notifier.key
        }
    }

    private fun removeNotifier(notifier: Notifier) {
        synchronized(notifiers) { notifiers -= notifier.key.watchable() }
    }

    override fun cancel(key: SmbWatchKey) {
        val notifier = synchronized(notifiers) { notifiers.remove(key.watchable())!! }
        notifier.interrupt()
        try {
            notifier.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun onClose() {
        // Don't keep synchronized on notifiers, or we may get a deadlock when joining.
        val notifiers = synchronized(notifiers) {
            notifiers.values.toList().also { notifiers.clear() }
        }
        var exception: IOException? = null
        for (notifier in notifiers) {
            notifier.interrupt()
            try {
                notifier.join()
            } catch (e: InterruptedException) {
                val newException = InterruptedIOException().apply { initCause(e) }
                if (exception == null) {
                    exception = newException
                } else {
                    exception.addSuppressed(newException)
                }
            }
        }
        exception?.let { throw it }
    }

    private class Notifier @Throws(IOException::class) constructor(
        private val watchService: SmbWatchService,
        path: SmbPath,
        @Volatile
        var kinds: Set<WatchEvent.Kind<*>>
    ) : Thread("SmbWatchService.Notifier-${id.getAndIncrement()}") {
        val key = SmbWatchKey(watchService, path)

        private val directory: Directory

        @Volatile
        private var future: Future<SMB2ChangeNotifyResponse>

        init {
            isDaemon = true
            try {
                directory = Client.openDirectoryForChangeNotification(path)
                future = Client.requestDirectoryChangeNotification(directory, COMPLETION_FILTER)
            } catch (e: ClientException) {
                throw e.toFileSystemException(path.toString())
            }
        }

        override fun run() {
            try {
                loop@ while (true) {
                    val response = future.get()
                    when (response.header.statusCode) {
                        NtStatus.STATUS_NOTIFY_ENUM_DIR.value ->
                            key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                        NtStatus.STATUS_SUCCESS.value -> {
                            if (FileSystemProviders.overflowWatchEvents) {
                                key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                            } else {
                                for (fileNotifyInfo in response.fileNotifyInfoList) {
                                    val kind = fileNotifyInfo.action.toEventKind()
                                    if (kind !in kinds) {
                                        continue
                                    }
                                    val name = key.watchable().fileSystem
                                        .getPath(fileNotifyInfo.fileName)
                                    key.addEvent(kind, name)
                                }
                            }
                        }
                        else ->
                            throw SMBApiException(
                                response.header, "Change notify failed for ${key.watchable()}"
                            )
                    }
                    future = Client.requestDirectoryChangeNotification(directory, COMPLETION_FILTER)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                key.setInvalid()
                if (!(e is InterruptedException || e is InterruptedIOException)) {
                    key.signal()
                }
                watchService.removeNotifier(this)
            } finally {
                // FIXME: We should cancel the CHANGE_NOTIFY request, but it currently crashes SMBJ.
                // https://github.com/hierynomus/smbj/issues/572
                // com.hierynomus.smbj.common.SMBRuntimeException:
                // Unknown SMB2 Message Command type: SMB2_CANCEL
                //future.cancel(true)
                directory.closeSafe()
            }
        }

        private fun FileNotifyAction.toEventKind(): WatchEvent.Kind<Path> =
            when (this) {
                FileNotifyAction.FILE_ACTION_ADDED, FileNotifyAction.FILE_ACTION_RENAMED_NEW_NAME ->
                    StandardWatchEventKinds.ENTRY_CREATE
                FileNotifyAction.FILE_ACTION_REMOVED,
                FileNotifyAction.FILE_ACTION_RENAMED_OLD_NAME ->
                    StandardWatchEventKinds.ENTRY_DELETE
                FileNotifyAction.FILE_ACTION_MODIFIED -> StandardWatchEventKinds.ENTRY_MODIFY
                else -> throw AssertionError(this)
            }

        companion object {
            private val COMPLETION_FILTER = setOf(
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_FILE_NAME,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_DIR_NAME,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_ATTRIBUTES,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_SIZE,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_LAST_WRITE,
                // We don't care about last access time and it might change too frequently.
                //SMB2CompletionFilter.FILE_NOTIFY_CHANGE_LAST_ACCESS,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_CREATION,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_EA,
                SMB2CompletionFilter.FILE_NOTIFY_CHANGE_SECURITY
            )

            private val id = AtomicInteger()
        }
    }
}
