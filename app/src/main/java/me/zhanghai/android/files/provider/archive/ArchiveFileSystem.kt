/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.provider.archive.archiver.ReadArchive
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPathCreator
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.files.provider.root.RootableFileSystem
import java.io.IOException
import java.io.InputStream

internal class ArchiveFileSystem(
    provider: ArchiveFileSystemProvider,
    archiveFile: Path
) : RootableFileSystem(
    { LocalArchiveFileSystem(it as ArchiveFileSystem, provider, archiveFile) },
    { RootArchiveFileSystem(it) }
), ByteStringListPathCreator {
    override val localFileSystem: LocalArchiveFileSystem
        get() = super.localFileSystem as LocalArchiveFileSystem

    override val rootFileSystem: RootArchiveFileSystem
        get() = super.rootFileSystem as RootArchiveFileSystem

    val rootDirectory: ArchivePath
        get() = localFileSystem.rootDirectory

    val defaultDirectory: ArchivePath
        get() = localFileSystem.defaultDirectory

    val archiveFile: Path
        get() = localFileSystem.archiveFile

    @Throws(IOException::class)
    fun getEntryAsLocal(path: Path): ReadArchive.Entry = localFileSystem.getEntry(path)

    @Throws(IOException::class)
    fun newInputStreamAsLocal(file: Path): InputStream = localFileSystem.newInputStream(file)

    @Throws(IOException::class)
    fun getDirectoryChildrenAsLocal(directory: Path): List<Path> =
        localFileSystem.getDirectoryChildren(directory)

    @Throws(IOException::class)
    fun readSymbolicLinkAsLocal(link: Path): String = localFileSystem.readSymbolicLink(link)

    fun addPassword(password: String) {
        localFileSystem.addPassword(password)
        rootFileSystem.addPassword(password)
    }

    fun setPasswords(passwords: List<String>) {
        localFileSystem.setPasswords(passwords)
        rootFileSystem.setPasswords(passwords)
    }

    fun refresh() {
        localFileSystem.refresh()
        rootFileSystem.refresh()
    }

    @Throws(RemoteFileSystemException::class)
    fun prepareAsRoot() {
        rootFileSystem.prepare()
    }

    override fun getPath(first: ByteString, vararg more: ByteString): ArchivePath =
        localFileSystem.getPath(first, *more)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(archiveFile as Parcelable, flags)
    }

    companion object {
        const val SEPARATOR: Byte = LocalArchiveFileSystem.SEPARATOR

        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchiveFileSystem> {
            override fun createFromParcel(source: Parcel): ArchiveFileSystem {
                val archiveFile = source.readParcelable<Parcelable>(Path::class.java.classLoader)
                    as Path
                return ArchiveFileSystemProvider.getOrNewFileSystem(archiveFile)
            }

            override fun newArray(size: Int): Array<ArchiveFileSystem?> = arrayOfNulls(size)
        }
    }
}
