package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import me.zhanghai.kotlin.filesystem.io.AsyncSink
import me.zhanghai.kotlin.filesystem.io.AsyncSource
import kotlin.coroutines.cancellation.CancellationException

public inline fun Path.Companion.fromPlatformPath(platformPath: ByteString): Path =
    FileSystemRegistry.platformFileSystem.getPath(platformPath)

public inline fun Path.Companion.fromPlatformPath(platformPath: String): Path =
    fromPlatformPath(platformPath.encodeToByteString())

public inline fun Path.toPlatformPath(): ByteString =
    FileSystemRegistry.platformFileSystem.toPlatformPath(this)

public inline fun Path.toPlatformPathString(): String = toPlatformPath().toString()

public inline fun Path.getOrCreateFileSystem(): FileSystem =
    FileSystemRegistry.getOrCreateFileSystem(this.rootUri)

public inline fun Path.toAbsolutePath(): Path =
    getOrCreateFileSystem().defaultDirectory.resolve(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.toRealPath(): Path = getOrCreateFileSystem().getRealPath(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.checkAccess(vararg modes: AccessMode) {
    getOrCreateFileSystem().checkAccess(this, *modes)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openMetadataView(
    vararg options: FileMetadataOption
): FileMetadataView = getOrCreateFileSystem().openMetadataView(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readMetadata(vararg options: FileMetadataOption): FileMetadata =
    getOrCreateFileSystem().readMetadata(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openContent(vararg options: FileContentOption): FileContent =
    getOrCreateFileSystem().openContent(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openSource(vararg options: FileContentOption): AsyncSource =
    getOrCreateFileSystem().openSource(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openSink(
    vararg options: FileContentOption = FileSystem.OPEN_SINK_OPTIONS_DEFAULT
): AsyncSink = getOrCreateFileSystem().openSink(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openDirectoryStream(
    vararg options: DirectoryStreamOption
): DirectoryStream = getOrCreateFileSystem().openDirectoryStream(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readDirectory(vararg options: DirectoryStreamOption): List<Path> =
    getOrCreateFileSystem().readDirectory(this, *options)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createDirectory(vararg options: CreateFileOption) {
    getOrCreateFileSystem().createDirectory(this, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.readSymbolicLink(): ByteString =
    getOrCreateFileSystem().readSymbolicLink(this)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createSymbolicLinkTo(
    target: ByteString,
    vararg options: CreateFileOption
) {
    getOrCreateFileSystem().createSymbolicLink(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.createHardLinkTo(existing: Path) {
    getOrCreateFileSystem().createHardLink(this, existing)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.delete() {
    getOrCreateFileSystem().delete(this)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.deleteIfExists(): Boolean =
    try {
        delete()
        true
    } catch (e: NoSuchFileException) {
        false
    }

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.isSameFileAs(other: Path): Boolean =
    getOrCreateFileSystem().isSameFile(this, other)

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.copyTo(target: Path, vararg options: CopyFileOption) {
    getOrCreateFileSystem().copy(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.moveTo(target: Path, vararg options: CopyFileOption) {
    getOrCreateFileSystem().move(this, target, *options)
}

@Throws(CancellationException::class, IOException::class)
public suspend inline fun Path.openFileStore(): FileStore =
    getOrCreateFileSystem().openFileStore(this)
