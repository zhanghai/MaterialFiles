/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.DocumentsContract
import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

/** @see DocumentsContract.Path */
internal class DocumentPath : ByteStringListPath<DocumentPath>, DocumentResolver.Path {
    private val fileSystem: DocumentFileSystem

    constructor(fileSystem: DocumentFileSystem, path: ByteString) : super(
        DocumentFileSystem.SEPARATOR, path
    ) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: DocumentFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(DocumentFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == DocumentFileSystem.SEPARATOR

    override fun createPath(path: ByteString): DocumentPath = DocumentPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): DocumentPath =
        DocumentPath(fileSystem, absolute, segments)

    override val uriSchemeSpecificPart: ByteString?
        get() = fileSystem.treeUri.toString().toByteString()

    override val uriFragment: ByteString?
        get() = super.uriSchemeSpecificPart

    override val defaultDirectory: DocumentPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): DocumentPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): DocumentPath {
        throw UnsupportedOperationException()
    }

    override fun toFile(): File {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun register(
        watcher: WatchService,
        events: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey {
        throw UnsupportedOperationException()
    }

    override val treeUri: Uri
        get() = fileSystem.treeUri

    override val displayName: String?
        get() = fileNameByteString?.toString()

    override val parent: DocumentPath?
        get() = getParent()

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DocumentPath> {
            override fun createFromParcel(source: Parcel): DocumentPath = DocumentPath(source)

            override fun newArray(size: Int): Array<DocumentPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isDocumentPath: Boolean
    get() = this is DocumentPath
