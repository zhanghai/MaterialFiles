/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.StrictMode
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.system.ErrnoException
import android.system.OsConstants
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessDeniedException
import java8.nio.file.FileSystemException
import java8.nio.file.FileSystemLoopException
import java8.nio.file.NoSuchFileException
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.StandardOpenOption
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.app.storageManager
import me.zhanghai.android.files.compat.ProxyFileDescriptorCallbackCompat
import me.zhanghai.android.files.compat.openProxyFileDescriptorCompat
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.force
import me.zhanghai.android.files.provider.common.getLastModifiedTime
import me.zhanghai.android.files.provider.common.isForceable
import me.zhanghai.android.files.provider.common.newByteChannel
import me.zhanghai.android.files.provider.common.size
import me.zhanghai.android.files.provider.document.documentUri
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.util.hasBits
import me.zhanghai.android.files.util.withoutPenaltyDeathOnNetwork
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException

class FileProvider : ContentProvider() {
    private lateinit var callbackThread: HandlerThread
    private lateinit var callbackHandler: Handler

    override fun onCreate(): Boolean {
        callbackThread = HandlerThread("FileProvider.CallbackThread")
        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)
        return true
    }

    override fun shutdown() {
        callbackThread.quitSafely()
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)

        if (info.exported) {
            throw SecurityException("Provider must not be exported")
        }
        if (!info.grantUriPermissions) {
            throw SecurityException("Provider must grant uri permissions")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        // ContentProvider has already checked granted permissions
        val projectionColumns = projection ?: getDefaultProjection()
        val path = uri.fileProviderPath
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        loop@ for (column in projectionColumns) {
            @Suppress("DEPRECATION")
            when (column) {
                OpenableColumns.DISPLAY_NAME -> {
                    columns += column
                    values += path.fileName.toString()
                }
                OpenableColumns.SIZE -> {
                    val size = try {
                        path.size()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                    columns += column
                    values += size
                }
                MediaStore.MediaColumns.DATA -> {
                    val file = try {
                        path.toFile()
                    } catch (e: UnsupportedOperationException) {
                        continue@loop
                    }
                    columns += column
                    values += file.absolutePath
                }
                // TODO: We should actually implement a DocumentsProvider since we are handling
                //  ACTION_OPEN_DOCUMENT.
                DocumentsContract.Document.COLUMN_MIME_TYPE -> {
                    columns += column
                    values += MimeType.guessFromPath(path.toString()).value
                }
                DocumentsContract.Document.COLUMN_LAST_MODIFIED -> {
                    val lastModified = try {
                        path.getLastModifiedTime().toMillis()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                    columns += column
                    values += lastModified
                }
            }
        }
        return MatrixCursor(columns.toTypedArray(), 1).apply {
            addRow(values)
        }
    }

    private fun getDefaultProjection(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && Binder.getCallingUid() == Process.SYSTEM_UID) {
            // com.android.internal.app.ChooserActivity.queryResolver() in Q queries with a null
            // projection (meaning all columns) on main thread but only actually needs the display
            // name (and document flags). However if we do return all the columns, we may perform
            // network requests and crash it due to StrictMode. So just work around by only
            // returning the display name in this case.
            CHOOSER_ACTIVITY_DEFAULT_PROJECTION
        } else {
            DEFAULT_PROJECTION
        }

    override fun getType(uri: Uri): String? {
        val path = uri.fileProviderPath
        return MimeType.guessFromPath(path.toString()).value
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("No external deletes")
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // ContentProvider has already checked granted permissions
        val path = uri.fileProviderPath
        val modeBits = ParcelFileDescriptor.parseMode(mode)
        if (path.canOpenDirectly(modeBits)) {
            return ParcelFileDescriptor.open(path.toFile(), modeBits)
        }
        val options = modeBits.toOpenOptions()
        val channel = try {
            // Strict mode thread policy is passed through binder, but some apps (notably music
            // players) like to open file on their main thread.
            StrictMode::class.withoutPenaltyDeathOnNetwork {
                path.newByteChannel(options)
            }
        } catch (e: IOException) {
            throw e.toFileNotFoundException()
        }
        return try {
            storageManager.openProxyFileDescriptorCompat(
                modeBits, ChannelCallback(channel), callbackHandler
            )
        } catch (e: IOException) {
            throw e.toFileNotFoundException()
        }
    }

    private fun Path.canOpenDirectly(mode: Int): Boolean {
        if (!isLinuxPath) {
            return false
        }
        val file = toFile()
        val readOnly = mode.hasBits(ParcelFileDescriptor.MODE_READ_ONLY)
        val writeOnly = mode.hasBits(ParcelFileDescriptor.MODE_WRITE_ONLY)
        val readWrite = mode.hasBits(ParcelFileDescriptor.MODE_READ_WRITE)
        val needRead = readOnly || readWrite
        val needWrite = writeOnly || readWrite
        return !((needRead && !file.canRead()) || (needWrite && !file.canWrite()))
    }

    private fun Int.toOpenOptions(): Set<OpenOption> =
        mutableSetOf<OpenOption>().apply {
            // May be "r" for read-only access, "rw" for read and write access, or "rwt" for
            // read and write access that truncates any existing file.
            require(!hasBits(ParcelFileDescriptor.MODE_APPEND)) { "mode ${this@toOpenOptions}" }
            if (hasBits(ParcelFileDescriptor.MODE_READ_ONLY)
                || hasBits(ParcelFileDescriptor.MODE_READ_WRITE)) {
                this += StandardOpenOption.READ
            }
            if (hasBits(ParcelFileDescriptor.MODE_WRITE_ONLY)
                || hasBits(ParcelFileDescriptor.MODE_READ_WRITE)) {
                this += StandardOpenOption.WRITE
            }
            if (hasBits(ParcelFileDescriptor.MODE_CREATE)) {
                this += StandardOpenOption.CREATE
            }
            if (hasBits(ParcelFileDescriptor.MODE_TRUNCATE)) {
                this += StandardOpenOption.TRUNCATE_EXISTING
            }
        }

    private fun IOException.toFileNotFoundException(): FileNotFoundException =
        if (this is FileNotFoundException) {
            this
        } else {
            FileNotFoundException(message).apply { initCause(this) }
        }

    private class ChannelCallback (
        private val channel: SeekableByteChannel
    ) : ProxyFileDescriptorCallbackCompat() {
        private var offset = 0L
        private var released = false

        @Throws(ErrnoException::class)
        override fun onGetSize(): Long {
            ensureNotReleased()
            return try {
                channel.size()
            } catch (e: IOException) {
                throw e.toErrnoException()
            }
        }

        @Throws(ErrnoException::class)
        override fun onRead(offset: Long, size: Int, data: ByteArray): Int {
            ensureNotReleased()
            if (this.offset != offset) {
                try {
                    channel.position(offset)
                } catch (e: IOException) {
                    throw e.toErrnoException()
                }
                this.offset = offset
            }
            val buffer = ByteBuffer.wrap(data, 0, size)
            // Unlike ReadableByteChannel which may not fill the buffer and returns -1 upon
            // end-of-stream, we need to read as much as we can unless end-of-stream is reached.
            while (buffer.hasRemaining()) {
                val channelSize = try {
                    channel.read(buffer)
                } catch (e: IOException) {
                    throw e.toErrnoException()
                }
                if (channelSize == -1) {
                    break
                }
                this.offset += channelSize
            }
            return (this.offset - offset).toInt()
        }

        @Throws(ErrnoException::class)
        override fun onWrite(offset: Long, size: Int, data: ByteArray): Int {
            ensureNotReleased()
            if (this.offset != offset) {
                try {
                    channel.position(offset)
                } catch (e: IOException) {
                    throw e.toErrnoException()
                }
                this.offset = offset
            }
            val buffer = ByteBuffer.wrap(data, 0, size)
            return try {
                channel.write(buffer)
            } catch (e: IOException) {
                throw e.toErrnoException()
            }.also { this.offset += it.toLong() }
        }

        @Throws(ErrnoException::class)
        override fun onFsync() {
            ensureNotReleased()
            if (channel.isForceable) {
                try {
                    channel.force(true)
                } catch (e: IOException) {
                    throw e.toErrnoException()
                }
            }
        }

        @Throws(ErrnoException::class)
        private fun ensureNotReleased() {
            if (released) {
                throw ErrnoException(null, OsConstants.EBADF)
            }
        }

        override fun onRelease() {
            if (released) {
                return
            }
            try {
                channel.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            released = true
        }

        private fun IOException.toErrnoException(): ErrnoException {
            val cause = cause
            return if (this is FileSystemException && cause is SyscallException) {
                ErrnoException(cause.functionName, cause.errno, this)
            } else {
                val errno = when (this) {
                    is AccessDeniedException -> OsConstants.EPERM
                    is FileSystemLoopException -> OsConstants.ELOOP
                    is InvalidFileNameException -> OsConstants.EINVAL
                    is IsDirectoryException -> OsConstants.EISDIR
                    is NoSuchFileException -> OsConstants.ENOENT
                    is ClosedByInterruptException, is InterruptedIOException -> OsConstants.EINTR
                    else -> OsConstants.EIO
                }
                ErrnoException(message, errno, this)
            }
        }
    }

    companion object {
        private val DEFAULT_PROJECTION = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATA,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        private val CHOOSER_ACTIVITY_DEFAULT_PROJECTION = arrayOf(
            OpenableColumns.DISPLAY_NAME
        )
    }
}

val Path.fileProviderUri: Uri
    get() {
        // Try avoid going through FUSE two times, which is bad for media playback.
        if (isDocumentPath) {
            try {
                return documentUri
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val uriPath = Uri.encode(toUri().toString())
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
            .path(uriPath)
            .build()
    }

private val Uri.fileProviderPath: Path
    get() {
        // Strip the prepended slash. A slash is always prepended because our Uri path starts with
        // our URI scheme, which can never start with a slash; but our Uri has an authority so its
        // path must start with a slash.
        val uriPath = Uri.decode(path).substring(1)
        return Paths.get(URI.create(uriPath))
    }
