package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import me.zhanghai.kotlin.filesystem.io.AsyncSink
import me.zhanghai.kotlin.filesystem.io.AsyncSource
import me.zhanghai.kotlin.filesystem.io.use
import me.zhanghai.kotlin.filesystem.io.withCloseable
import kotlin.coroutines.cancellation.CancellationException

public interface FileSystem {
    public val rootUri: Uri

    public val rootDirectory: Path
        get() = Path.of(rootUri, true, emptyList())

    public val defaultDirectory: Path

    @Throws(CancellationException::class, IOException::class)
    public suspend fun getRealPath(path: Path): Path

    @Throws(CancellationException::class, IOException::class)
    public suspend fun checkAccess(path: Path, vararg modes: AccessMode)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openMetadataView(
        file: Path,
        vararg options: FileMetadataOption
    ): FileMetadataView

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readMetadata(file: Path, vararg options: FileMetadataOption): FileMetadata =
        openMetadataView(file, *options).use { it.readMetadata() }

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openContent(file: Path, vararg options: FileContentOption): FileContent

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openSource(file: Path, vararg options: FileContentOption): AsyncSource {
        require(BasicFileContentOption.WRITE !in options) { BasicFileContentOption.WRITE }
        require(BasicFileContentOption.APPEND !in options) { BasicFileContentOption.APPEND }
        return openContent(file, *options).let { it.openSource().withCloseable(it) }
    }

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openSink(
        file: Path,
        vararg options: FileContentOption = OPEN_SINK_OPTIONS_DEFAULT
    ): AsyncSink {
        require(BasicFileContentOption.READ !in options) { BasicFileContentOption.READ }
        require(
            BasicFileContentOption.WRITE in options || BasicFileContentOption.APPEND in options
        ) {
            "Missing ${BasicFileContentOption.WRITE} or ${BasicFileContentOption.APPEND}"
        }
        return openContent(file, *options).let { it.openSink().withCloseable(it) }
    }

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openDirectoryStream(
        directory: Path,
        vararg options: DirectoryStreamOption
    ): DirectoryStream

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readDirectory(
        directory: Path,
        vararg options: DirectoryStreamOption
    ): List<Path> =
        openDirectoryStream(directory, *options).use { directoryStream ->
            buildList {
                while (true) {
                    val directoryEntry = directoryStream.read() ?: break
                    this += directory.resolve(directoryEntry.name)
                }
            }
        }

    @Throws(CancellationException::class, IOException::class)
    public suspend fun createDirectory(directory: Path, vararg options: CreateFileOption)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readSymbolicLink(link: Path): ByteString

    @Throws(CancellationException::class, IOException::class)
    public suspend fun createSymbolicLink(
        link: Path,
        target: ByteString,
        vararg options: CreateFileOption
    )

    @Throws(CancellationException::class, IOException::class)
    public suspend fun createHardLink(link: Path, existing: Path)

    @Throws(CancellationException::class, IOException::class) public suspend fun delete(path: Path)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun isSameFile(path1: Path, path2: Path): Boolean

    @Throws(CancellationException::class, IOException::class)
    public suspend fun copy(source: Path, target: Path, vararg options: CopyFileOption)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun move(source: Path, target: Path, vararg options: CopyFileOption)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun openFileStore(path: Path): FileStore

    public companion object {
        @PublishedApi
        internal val OPEN_SINK_OPTIONS_DEFAULT: Array<FileContentOption> =
            arrayOf(
                BasicFileContentOption.WRITE,
                BasicFileContentOption.TRUNCATE_EXISTING,
                BasicFileContentOption.CREATE
            )
    }
}
