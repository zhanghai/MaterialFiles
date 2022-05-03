/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document

import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.provider.common.AbstractPathObservable
import me.zhanghai.android.files.provider.content.resolver.ResolverException
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver

internal class DocumentPathObservable(
    path: DocumentPath,
    intervalMillis: Long
) : AbstractPathObservable(intervalMillis) {
    private val cursor: Cursor

    private val contentObserver = object : ContentObserver(handler) {
        override fun deliverSelfNotifications(): Boolean = true

        override fun onChange(selfChange: Boolean) {
            notifyObservers()
        }
    }

    init {
        val uri = try {
            path.observableUri
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        cursor = try {
            DocumentResolver.query(uri, emptyArray(), null)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        cursor.registerContentObserver(contentObserver)
    }

    override fun onCloseLocked() {
        cursor.unregisterContentObserver(contentObserver)
        cursor.close()
    }

    private val DocumentPath.observableUri: Uri
        @Throws(ResolverException::class)
        get() {
            // Querying children for a regular file is fine for non-directory since API 29, but for
            // older APIs we'll have to work around by observing all children of its parent.
            // @see com.android.internal.content.FileSystemProvider#queryChildDocuments(String,
            //      String[], String)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val mimeType = DocumentResolver.getMimeType(this)
                if (mimeType != MimeType.DIRECTORY.value) {
                    parent?.let { return DocumentResolver.getDocumentChildrenUri(it) }
                }
            }
            return DocumentResolver.getDocumentChildrenUri(this)
        }
}
