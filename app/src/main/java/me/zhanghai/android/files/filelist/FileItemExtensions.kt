/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.content.Context
import android.provider.DocumentsContract
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.getBrokenSymbolicLinkName
import me.zhanghai.android.files.file.getName
import me.zhanghai.android.files.file.isMedia
import me.zhanghai.android.files.file.supportsThumbnail
import me.zhanghai.android.files.provider.archive.createArchiveRootPath
import me.zhanghai.android.files.provider.document.DocumentFileAttributes
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.FileNameUtils
import me.zhanghai.android.files.util.hasBits
import me.zhanghai.android.files.util.valueCompat

val FileItem.name: String
    get() = path.name

val FileItem.extension: String
    get() = if (attributes.isDirectory) "" else FileNameUtils.getExtension(name)

fun FileItem.getMimeTypeName(context: Context): String {
        if (attributesNoFollowLinks.isSymbolicLink && isSymbolicLinkBroken) {
            return MimeType.getBrokenSymbolicLinkName(context)
        }
        return mimeType.getName(extension, context)
    }

val FileItem.isArchiveFile: Boolean
    get() = path.isArchiveFile(mimeType)

val FileItem.isListable: Boolean
    get() = attributes.isDirectory || isArchiveFile

val FileItem.listablePath: Path
    get() = if (isArchiveFile) path.createArchiveRootPath() else path

val FileItem.supportsThumbnail: Boolean
    get() =
        when {
            path.isLinuxPath -> mimeType.supportsThumbnail
            path.isDocumentPath -> {
                val attributes = attributes as DocumentFileAttributes
                when {
                    attributes.flags.hasBits(DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL) ->
                        true
                    mimeType.isMedia ->
                        DocumentResolver.isLocal(path as DocumentResolver.Path)
                            || Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.valueCompat
                    else -> false
                }
            }
            // TODO: Allow other providers as well - but might be resource consuming.
            else -> false
        }

fun FileItem.createDummyArchiveRoot(): FileItem =
    FileItem(
        path.createArchiveRootPath(), DummyArchiveRootBasicFileAttributes(), null, null, false,
        MimeType.DIRECTORY
    )

// Dummy attributes only to be added to the selection set, which may be used to determine file
// type when confirming deletion.
private class DummyArchiveRootBasicFileAttributes : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun lastAccessTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun creationTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun isRegularFile(): Boolean = false

    override fun isDirectory(): Boolean = true

    override fun isSymbolicLink(): Boolean = false

    override fun isOther(): Boolean = false

    override fun size(): Long {
        throw UnsupportedOperationException()
    }

    override fun fileKey(): Any {
        throw UnsupportedOperationException()
    }
}
