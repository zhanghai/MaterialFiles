/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.os.Parcelable
import androidx.annotation.WorkerThread
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector
import me.zhanghai.android.files.provider.common.isHidden
import me.zhanghai.android.files.provider.common.readAttributes
import me.zhanghai.android.files.provider.common.readSymbolicLinkByteString
import me.zhanghai.android.files.util.ParcelableParceler
import java.io.IOException

@Parcelize
data class FileItem(
    val path: @WriteWith<ParcelableParceler> Path,
    val attributesNoFollowLinks: @WriteWith<ParcelableParceler> BasicFileAttributes,
    val symbolicLinkTarget: String?,
    private val symbolicLinkTargetAttributes: @WriteWith<ParcelableParceler> BasicFileAttributes?,
    val isHidden: Boolean,
    val mimeType: MimeType
) : Parcelable {
    val attributes: BasicFileAttributes
        get() = symbolicLinkTargetAttributes ?: attributesNoFollowLinks

    val isSymbolicLinkBroken: Boolean
        get() {
            check(attributesNoFollowLinks.isSymbolicLink) { "Not a symbolic link" }
            return symbolicLinkTargetAttributes == null
        }
}

@WorkerThread
@Throws(IOException::class)
fun Path.loadFileItem(): FileItem {
    val attributes = readAttributes(BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
    val isHidden = isHidden
    if (!attributes.isSymbolicLink) {
        val mimeType = AndroidFileTypeDetector.getMimeType(this, attributes).asMimeType()
        return FileItem(this, attributes, null, null, isHidden, mimeType)
    }
    val symbolicLinkTarget = readSymbolicLinkByteString().toString()
    val symbolicLinkTargetAttributes = try {
        readAttributes(BasicFileAttributes::class.java)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
    val mimeType = AndroidFileTypeDetector.getMimeType(
        this, symbolicLinkTargetAttributes ?: attributes
    ).asMimeType()
    return FileItem(
        this, attributes, symbolicLinkTarget, symbolicLinkTargetAttributes, isHidden, mimeType
    )
}
