/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.documentsprovider

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import androidx.annotation.RequiresApi
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileVisitResult
import java8.nio.file.Files
import java8.nio.file.Path
import java8.nio.file.SimpleFileVisitor
import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.loadFileItem
import me.zhanghai.android.files.file.openParcelFileDescriptor
import me.zhanghai.android.files.provider.common.createDirectory
import me.zhanghai.android.files.provider.common.createFile
import me.zhanghai.android.files.provider.common.delete
import me.zhanghai.android.files.provider.common.exists
import me.zhanghai.android.files.provider.common.isDirectory
import me.zhanghai.android.files.provider.common.moveTo
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.Storage
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.withoutPenaltyDeathOnNetwork
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Exposes every location configured in Material Files (local storage, SMB, SFTP, FTP, WebDAV and
 * archives) through the Storage Access Framework, so that other apps can open, create and keep
 * persistent access to documents and folders via the system picker or our own picker.
 *
 * Paths that are only reachable with root or Shizuku are gated by [isExternalAccessAllowed].
 */
class FilesDocumentsProvider : DocumentsProvider() {
    private lateinit var callbackThread: HandlerThread
    private lateinit var callbackHandler: Handler

    override fun onCreate(): Boolean {
        callbackThread = HandlerThread("FilesDocumentsProvider.CallbackThread")
        callbackThread.start()
        callbackHandler = Handler(callbackThread.looper)
        // Providers are created before Application.onCreate(), and LiveData must be observed on
        // the main thread.
        Handler(Looper.getMainLooper()).post {
            Settings.STORAGES.observeForever { notifyRootsChanged() }
            Settings.ROOT_STRATEGY.observeForever { notifyRootsChanged() }
            Settings.EXTERNAL_ROOT_ACCESS.observeForever { notifyRootsChanged() }
        }
        return true
    }

    override fun shutdown() {
        callbackThread.quitSafely()
    }

    override fun queryRoots(projection: Array<String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)
        val context = context!!
        val callingPackage = callingPackageOrNull
        for (storage in Settings.STORAGES.valueCompat) {
            val path = storage.exportedPath ?: continue
            if (!isExternalAccessAllowed(callingPackage, path)) {
                continue
            }
            var flags = Root.FLAG_SUPPORTS_IS_CHILD
            if (!path.fileSystem.isReadOnly) {
                flags = flags or Root.FLAG_SUPPORTS_CREATE
            }
            if (path.isLinuxPath) {
                flags = flags or Root.FLAG_LOCAL_ONLY
            }
            cursor.newRow()
                .add(Root.COLUMN_ROOT_ID, storage.id.toString())
                .add(Root.COLUMN_DOCUMENT_ID, path.documentId)
                .add(Root.COLUMN_TITLE, storage.getName(context))
                .add(Root.COLUMN_SUMMARY, storage.description)
                // Storage icons are white and meant for our own UI; use the app icon so that our
                // roots are recognizable in the system picker.
                .add(Root.COLUMN_ICON, R.mipmap.launcher_icon)
                .add(Root.COLUMN_FLAGS, flags)
        }
        return cursor
    }

    @Throws(FileNotFoundException::class)
    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor =
        runIo {
            val path = documentId.toAccessiblePath()
            MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION).apply { addPath(path) }
        }

    @Throws(FileNotFoundException::class)
    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<String>?,
        sortOrder: String?
    ): Cursor =
        runIo {
            val parent = parentDocumentId.toAccessiblePath()
            val callingPackage = callingPackageOrNull
            val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
            parent.newDirectoryStream().use { directoryStream ->
                for (child in directoryStream) {
                    if (!isExternalAccessAllowed(callingPackage, child)) {
                        continue
                    }
                    try {
                        cursor.addPath(child)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            cursor.setNotificationUri(
                context!!.contentResolver, childDocumentsUri(parentDocumentId)
            )
            cursor
        }

    @Throws(FileNotFoundException::class)
    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val path = runIo {
            documentId.toAccessiblePath().also {
                if (it.isDirectory()) {
                    throw FileNotFoundException("Cannot open a directory: $documentId")
                }
            }
        }
        return path.openParcelFileDescriptor(mode, callbackHandler)
    }

    @Throws(FileNotFoundException::class)
    override fun createDocument(
        parentDocumentId: String,
        mimeType: String,
        displayName: String
    ): String =
        runIo {
            val parent = parentDocumentId.toAccessiblePath()
            val path = parent.resolveUniqueChild(displayName)
            path.enforceExternalAccess()
            if (mimeType == Document.MIME_TYPE_DIR) {
                path.createDirectory()
            } else {
                path.createFile()
            }
            notifyChildrenChanged(parentDocumentId)
            path.documentId
        }

    @Throws(FileNotFoundException::class)
    override fun deleteDocument(documentId: String) {
        runIo {
            val path = documentId.toAccessiblePath()
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attributes: BasicFileAttributes):
                    FileVisitResult {
                    file.delete()
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(directory: Path, exception: IOException?):
                    FileVisitResult {
                    if (exception != null) {
                        throw exception
                    }
                    directory.delete()
                    return FileVisitResult.CONTINUE
                }
            })
            revokeDocumentPermission(documentId)
            path.parent?.let { notifyChildrenChanged(it.documentId) }
        }
    }

    @Throws(FileNotFoundException::class)
    override fun renameDocument(documentId: String, displayName: String): String? =
        runIo {
            val path = documentId.toAccessiblePath()
            val target = path.resolveSibling(displayName)
            if (target == path) {
                return@runIo null
            }
            if (target.exists()) {
                throw FileAlreadyExistsException(target.toString())
            }
            target.enforceExternalAccess()
            path.moveTo(target)
            path.parent?.let { notifyChildrenChanged(it.documentId) }
            target.documentId
        }

    @Throws(FileNotFoundException::class)
    override fun getDocumentType(documentId: String): String =
        runIo { documentId.toAccessiblePath().loadFileItem().documentMimeType }

    override fun isChildDocument(parentDocumentId: String, documentId: String): Boolean {
        val parent = parentDocumentId.toDocumentPathOrNull() ?: return false
        val path = documentId.toDocumentPathOrNull() ?: return false
        return path.isSameOrDescendantOf(parent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(FileNotFoundException::class)
    override fun findDocumentPath(
        parentDocumentId: String?,
        childDocumentId: String
    ): DocumentsContract.Path {
        val path = childDocumentId.toAccessiblePath().normalize()
        if (parentDocumentId != null) {
            val parent = parentDocumentId.toDocumentPath()
            if (!path.isSameOrDescendantOf(parent)) {
                throw FileNotFoundException("$childDocumentId is not under $parentDocumentId")
            }
            return DocumentsContract.Path(null, path.documentIdsFrom(parent))
        }
        val storage = Settings.STORAGES.valueCompat
            .filter { storage ->
                storage.exportedPath?.let { path.isSameOrDescendantOf(it) } ?: false
            }
            .maxByOrNull { it.exportedPath!!.nameCount }
            ?: throw FileNotFoundException("No root contains $childDocumentId")
        return DocumentsContract.Path(
            storage.id.toString(), path.documentIdsFrom(storage.exportedPath!!)
        )
    }

    private fun Path.documentIdsFrom(ancestor: Path): List<String> =
        generateSequence(this) { it.parent }
            .takeWhile { it.isSameOrDescendantOf(ancestor) }
            .map { it.documentId }
            .toList()
            .asReversed()

    @Throws(IOException::class)
    private fun MatrixCursor.addPath(path: Path) {
        val file = path.loadFileItem()
        val attributes = file.attributes
        val isDirectory = attributes.isDirectory
        var flags = 0
        if (!path.fileSystem.isReadOnly) {
            flags = flags or Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_RENAME
            flags = if (isDirectory) {
                flags or Document.FLAG_DIR_SUPPORTS_CREATE
            } else {
                flags or Document.FLAG_SUPPORTS_WRITE
            }
        }
        newRow()
            .add(Document.COLUMN_DOCUMENT_ID, path.documentId)
            .add(Document.COLUMN_DISPLAY_NAME, path.fileName?.toString() ?: path.toString())
            .add(Document.COLUMN_MIME_TYPE, file.documentMimeType)
            .add(Document.COLUMN_SIZE, if (isDirectory) null else attributes.size())
            .add(Document.COLUMN_LAST_MODIFIED, attributes.lastModifiedTime().toMillis())
            .add(Document.COLUMN_FLAGS, flags)
    }

    private val FileItem.documentMimeType: String
        get() = if (attributes.isDirectory) Document.MIME_TYPE_DIR else mimeType.value

    @Throws(IOException::class)
    private fun Path.resolveUniqueChild(displayName: String): Path {
        var child = resolve(displayName)
        if (!child.exists()) {
            return child
        }
        val extensionIndex = displayName.lastIndexOf('.').takeIf { it > 0 } ?: displayName.length
        val baseName = displayName.substring(0, extensionIndex)
        val extension = displayName.substring(extensionIndex)
        var number = 1
        while (child.exists()) {
            child = resolve("$baseName ($number)$extension")
            ++number
        }
        return child
    }

    @Throws(FileNotFoundException::class)
    private fun String.toAccessiblePath(): Path = toDocumentPath().enforceExternalAccess()

    private fun Path.enforceExternalAccess(): Path {
        val callingPackage = callingPackageOrNull
        if (!isExternalAccessAllowed(callingPackage, this)) {
            throw SecurityException(
                "$callingPackage is not allowed to access $this with root or Shizuku"
            )
        }
        return this
    }

    private val callingPackageOrNull: String?
        get() =
            try {
                callingPackage
            } catch (e: SecurityException) {
                null
            }

    private val Storage.exportedPath: Path?
        // Document trees from other providers are already in the system picker.
        get() = if (isVisible) path?.takeUnless { it.isDocumentPath } else null

    private fun childDocumentsUri(parentDocumentId: String): Uri =
        DocumentsContract.buildChildDocumentsUri(AUTHORITY, parentDocumentId)

    private fun notifyChildrenChanged(parentDocumentId: String) {
        context?.contentResolver?.notifyChange(childDocumentsUri(parentDocumentId), null)
    }

    private fun notifyRootsChanged() {
        context?.contentResolver?.notifyChange(DocumentsContract.buildRootsUri(AUTHORITY), null)
    }

    /**
     * Runs file system I/O for another app: network access is allowed even if the caller's strict
     * mode policy (which is passed through binder) forbids it, and [IOException]s are converted to
     * the [FileNotFoundException] that [DocumentsProvider] methods are allowed to throw.
     */
    @Throws(FileNotFoundException::class)
    private inline fun <R> runIo(crossinline block: () -> R): R =
        try {
            StrictMode::class.withoutPenaltyDeathOnNetwork { block() }
        } catch (e: FileNotFoundException) {
            throw e
        } catch (e: IOException) {
            throw FileNotFoundException(e.message).apply { initCause(e) }
        }

    companion object {
        const val AUTHORITY = BuildConfig.DOCUMENTS_PROVIDER_AUTHORITY

        private val DEFAULT_ROOT_PROJECTION = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_ICON,
            Root.COLUMN_FLAGS
        )

        private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS
        )
    }
}
