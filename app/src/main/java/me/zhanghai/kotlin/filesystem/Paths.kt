package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import me.zhanghai.kotlin.filesystem.io.Sink
import me.zhanghai.kotlin.filesystem.io.Source
import kotlin.coroutines.cancellation.CancellationException

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.getFileSystem(): FileSystem = FileSystemRegistry.getFileSystem(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openMetadataView(
    vararg options: FileMetadataOption
): FileMetadataView = getFileSystem().openMetadataView(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readMetadata(vararg options: FileMetadataOption): FileMetadata =
    getFileSystem().readMetadata(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openContent(vararg options: FileContentOption): FileContent =
    getFileSystem().openContent(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openSource(vararg options: FileContentOption): Source =
    getFileSystem().openSource(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openSink(
    vararg options: FileContentOption = FileSystem.OPEN_SINK_OPTIONS_DEFAULT
): Sink = getFileSystem().openSink(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openDirectoryStream(
    vararg options: DirectoryStreamOption
): DirectoryStream = getFileSystem().openDirectoryStream(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readDirectory(vararg options: DirectoryStreamOption): List<Path> =
    getFileSystem().readDirectory(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createDirectory(vararg options: CreateFileOption) {
    getFileSystem().createDirectory(this, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readSymbolicLink(): ByteString =
    getFileSystem().readSymbolicLink(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createSymbolicLinkTo(
    target: ByteString,
    vararg options: CreateFileOption
) {
    getFileSystem().createSymbolicLink(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createHardLinkTo(existing: Path) {
    getFileSystem().createHardLink(this, existing)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.delete() {
    getFileSystem().delete(this)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.deleteIfExists(): Boolean = getFileSystem().deleteIfExists(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.copyTo(target: Path, vararg options: CopyFileOption) {
    getFileSystem().copy(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.moveTo(target: Path, vararg options: CopyFileOption) {
    getFileSystem().move(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.isSameFileAs(other: Path): Boolean =
    getFileSystem().isSameFile(this, other)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.checkAccess(vararg modes: AccessMode) {
    getFileSystem().checkAccess(this, *modes)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openFileStore(): FileStore = getFileSystem().openFileStore(this)
