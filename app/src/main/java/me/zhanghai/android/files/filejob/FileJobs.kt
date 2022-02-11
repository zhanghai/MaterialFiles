/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.AnyRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryIteratorException
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileVisitResult
import java8.nio.file.FileVisitor
import java8.nio.file.Files
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.SimpleFileVisitor
import java8.nio.file.StandardCopyOption
import java8.nio.file.StandardOpenOption
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.BackgroundActivityStarter
import me.zhanghai.android.files.app.mainExecutor
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asFileSize
import me.zhanghai.android.files.file.fileProviderUri
import me.zhanghai.android.files.file.loadFileItem
import me.zhanghai.android.files.filelist.OpenFileAsDialogActivity
import me.zhanghai.android.files.filelist.OpenFileAsDialogFragment
import me.zhanghai.android.files.provider.archive.archiveFile
import me.zhanghai.android.files.provider.archive.archiver.ArchiveWriter
import me.zhanghai.android.files.provider.archive.createArchiveRootPath
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixFileStore
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixPrincipal
import me.zhanghai.android.files.provider.common.PosixUser
import me.zhanghai.android.files.provider.common.ProgressCopyOption
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException
import me.zhanghai.android.files.provider.common.asByteStringListPath
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.createDirectories
import me.zhanghai.android.files.provider.common.createDirectory
import me.zhanghai.android.files.provider.common.createFile
import me.zhanghai.android.files.provider.common.delete
import me.zhanghai.android.files.provider.common.deleteIfExists
import me.zhanghai.android.files.provider.common.exists
import me.zhanghai.android.files.provider.common.getFileStore
import me.zhanghai.android.files.provider.common.getMode
import me.zhanghai.android.files.provider.common.getPath
import me.zhanghai.android.files.provider.common.isDirectory
import me.zhanghai.android.files.provider.common.moveTo
import me.zhanghai.android.files.provider.common.newByteChannel
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.provider.common.readAttributes
import me.zhanghai.android.files.provider.common.resolveForeign
import me.zhanghai.android.files.provider.common.restoreSeLinuxContext
import me.zhanghai.android.files.provider.common.setGroup
import me.zhanghai.android.files.provider.common.setMode
import me.zhanghai.android.files.provider.common.setOwner
import me.zhanghai.android.files.provider.common.setSeLinuxContext
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.common.toModeString
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.util.asFileName
import me.zhanghai.android.files.util.createInstallPackageIntent
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.getQuantityString
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.toEnumSet
import me.zhanghai.android.files.util.withChooser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun FileJob.getString(@StringRes stringRes: Int): String {
    return service.getString(stringRes)
}

fun FileJob.getString(@StringRes stringRes: Int, vararg formatArguments: Any?): String {
    return service.getString(stringRes, *formatArguments)
}

fun FileJob.getQuantityString(@PluralsRes pluralRes: Int, quantity: Int): String {
    return service.getQuantityString(pluralRes, quantity)
}

fun FileJob.getQuantityString(
    @PluralsRes pluralRes: Int,
    quantity: Int,
    vararg formatArguments: Any?
): String {
    return service.getQuantityString(pluralRes, quantity, *formatArguments)
}

private fun FileJob.postNotification(
    title: CharSequence,
    text: CharSequence?,
    subText: CharSequence?,
    info: CharSequence?,
    max: Int,
    progress: Int,
    indeterminate: Boolean,
    showCancel: Boolean
) {
    val notification = fileJobNotificationTemplate.createBuilder(service).apply {
        setContentTitle(title)
        setContentText(text)
        setSubText(subText)
        setContentInfo(info)
        setProgress(max, progress, indeterminate)
        // TODO
        //setContentIntent()
        if (showCancel) {
            val intent = FileJobReceiver.createIntent(id)
            var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                service, id + 1, intent, pendingIntentFlags
            )
            addAction(
                R.drawable.close_icon_white_24dp, getString(android.R.string.cancel), pendingIntent
            )
        }
    }.build()
    service.notificationManager.notify(id, notification)
}

private const val PROGRESS_INTERVAL_MILLIS = 200L

private const val NOTIFICATION_INTERVAL_MILLIS = 500L

private fun FileJob.getFileName(path: Path): String =
    if (path.isAbsolute && path.nameCount == 0) {
        path.fileSystem.separator
    } else {
        path.fileName.toString()
    }

private fun FileJob.getTargetFileName(source: Path): Path {
    if (source.isArchivePath) {
        val archiveFile = source.archiveFile.asByteStringListPath()
        val archiveRoot = archiveFile.createArchiveRootPath()
        if (source == archiveRoot) {
            return archiveFile.fileSystem.getPath(
                archiveFile.fileNameByteString!!.asFileName().baseName
            )
        }
    }
    return source.fileName
}

// The attributes for start path prefers following links, but falls back to not following.
// FileVisitResult returned from visitor may be ignored and always considered CONTINUE.
@Throws(IOException::class)
private fun FileJob.walkFileTreeForSettingAttributes(
    start: Path,
    recursive: Boolean,
    visitor: FileVisitor<in Path>
): Path {
    val attributes = try {
        start.readAttributes(BasicFileAttributes::class.java)
    } catch (ignored: IOException) {
        try {
            start.readAttributes(BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        } catch (e: IOException) {
            visitor.visitFileFailed(start, e)
            return start
        }
    }
    if (!recursive || !attributes.isDirectory) {
        visitor.visitFile(start, attributes)
        return start
    }
    val directoryStream = try {
        start.newDirectoryStream()
    } catch (e: IOException) {
        visitor.visitFileFailed(start, e)
        return start
    }
    directoryStream.use {
        visitor.preVisitDirectory(start, attributes)
        try {
            directoryStream.forEach { Files.walkFileTree(it, visitor) }
        } catch (e: DirectoryIteratorException) {
            visitor.postVisitDirectory(start, e.cause)
            return start
        }
    }
    visitor.postVisitDirectory(start, null)
    return start
}

@Throws(InterruptedIOException::class)
private fun FileJob.throwIfInterrupted() {
    if (Thread.interrupted()) {
        throw InterruptedIOException()
    }
}

@Throws(IOException::class)
private fun FileJob.scan(sources: List<Path?>, @PluralsRes notificationTitleRes: Int): ScanInfo {
    val scanInfo = ScanInfo()
    for (source in sources) {
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                scanPath(attributes, scanInfo, notificationTitleRes)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                scanPath(attributes, scanInfo, notificationTitleRes)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }
        })
    }
    postScanNotification(scanInfo, notificationTitleRes)
    return scanInfo
}

@Throws(IOException::class)
private fun FileJob.scan(source: Path, @PluralsRes notificationTitleRes: Int): ScanInfo {
    return scan(listOf(source), notificationTitleRes)
}

@Throws(IOException::class)
private fun FileJob.scan(
    source: Path,
    recursive: Boolean,
    @PluralsRes notificationTitleRes: Int
): ScanInfo {
    if (recursive) {
        return scan(source, notificationTitleRes)
    }
    val scanInfo = ScanInfo()
    val attributes = source.readAttributes(
        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
    )
    scanPath(attributes, scanInfo, notificationTitleRes)
    throwIfInterrupted()
    return scanInfo
}

private fun FileJob.scanPath(
    attributes: BasicFileAttributes,
    scanInfo: ScanInfo,
    @PluralsRes notificationTitleRes: Int
) {
    scanInfo.incrementFileCount()
    scanInfo.addToSize(attributes.size())
    postScanNotification(scanInfo, notificationTitleRes)
}

private fun FileJob.postScanNotification(scanInfo: ScanInfo, @PluralsRes titleRes: Int) {
    if (!scanInfo.shouldPostNotification()) {
        return
    }
    val size = scanInfo.size.asFileSize().formatHumanReadable(service)
    val fileCount: Int = scanInfo.fileCount
    val title: String = getQuantityString(titleRes, fileCount, fileCount, size)
    postNotification(title, null, null, null, 0, 0, true, true)
}

private class ScanInfo {
    var fileCount = 0
        private set
    var size = 0L
        private set

    private var lastNotificationTimeMillis = 0L

    fun incrementFileCount() {
        ++fileCount
    }

    fun addToSize(size: Long) {
        this.size += size
    }

    fun shouldPostNotification(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return if (fileCount % 100 == 0
            || lastNotificationTimeMillis + NOTIFICATION_INTERVAL_MILLIS < currentTimeMillis) {
            lastNotificationTimeMillis = currentTimeMillis
            true
        } else {
            false
        }
    }
}

private fun FileJob.postTransferSizeNotification(
    transferInfo: TransferInfo,
    currentSource: Path,
    @StringRes titleOneRes: Int,
    @PluralsRes titleMultipleRes: Int
) {
    if (!transferInfo.shouldPostNotification()) {
        return
    }
    val title: String
    val text: String
    val fileCount = transferInfo.fileCount
    val target = transferInfo.target!!
    val size = transferInfo.size
    val transferredSize = transferInfo.transferredSize
    if (fileCount == 1) {
        title = getString(titleOneRes, getFileName(currentSource), getFileName(target))
        val sizeString = size.asFileSize().formatHumanReadable(service)
        val transferredSizeString = transferredSize.asFileSize().formatHumanReadable(service)
        text = getString(
            R.string.file_job_transfer_size_notification_text_one_format, transferredSizeString,
            sizeString
        )
    } else {
        title = getQuantityString(titleMultipleRes, fileCount, fileCount, getFileName(target))
        val currentFileIndex = (transferInfo.transferredFileCount + 1)
            .coerceAtMost(fileCount)
        text = getString(
            R.string.file_job_transfer_size_notification_text_multiple_format, currentFileIndex,
            fileCount
        )
    }
    val max: Int
    val progress: Int
    if (size <= Int.MAX_VALUE) {
        max = size.toInt()
        progress = transferredSize.toInt()
    } else {
        var maxLong = size
        var progressLong = transferredSize
        while (maxLong > Int.MAX_VALUE) {
            maxLong /= 2
            progressLong /= 2
        }
        max = maxLong.toInt()
        progress = progressLong.toInt()
    }
    postNotification(title, text, null, null, max, progress, false, true)
}

private fun FileJob.postTransferCountNotification(
    transferInfo: TransferInfo,
    currentPath: Path,
    @StringRes titleOneRes: Int,
    @PluralsRes titleMultipleRes: Int
) {
    if (!transferInfo.shouldPostNotification()) {
        return
    }
    val title: String
    val text: String?
    val max: Int
    val progress: Int
    val indeterminate: Boolean
    val fileCount = transferInfo.fileCount
    if (fileCount == 1) {
        title = getString(titleOneRes, getFileName(currentPath))
        text = null
        max = 0
        progress = 0
        indeterminate = true
    } else {
        title = getQuantityString(titleMultipleRes, fileCount, fileCount)
        val transferredFileCount = transferInfo.transferredFileCount
        val currentFileIndex = (transferredFileCount + 1).coerceAtMost(fileCount)
        text = getString(
            R.string.file_job_transfer_count_notification_text_multiple_format, currentFileIndex,
            fileCount
        )
        max = fileCount
        progress = transferredFileCount
        indeterminate = false
    }
    postNotification(title, text, null, null, max, progress, indeterminate, true)
}

private class TransferInfo(scanInfo: ScanInfo, val target: Path?) {
    var fileCount: Int = scanInfo.fileCount
        private set
    var transferredFileCount = 0
        private set
    var size: Long = scanInfo.size
        private set
    var transferredSize = 0L
        private set

    private var lastNotificationTimeMillis = 0L

    fun incrementTransferredFileCount() {
        ++transferredFileCount
    }

    fun addTransferredFile(path: Path) {
        ++transferredFileCount
        try {
            transferredSize += path.readAttributes(
                BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
            ).size()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun skipFile(path: Path) {
        --fileCount
        try {
            size -= path.readAttributes(
                BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
            ).size()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun skipFileIgnoringSize() {
        --fileCount
    }

    fun addToTransferredSize(size: Long) {
        transferredSize += size
    }

    fun shouldPostNotification(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return if (lastNotificationTimeMillis + NOTIFICATION_INTERVAL_MILLIS < currentTimeMillis) {
            lastNotificationTimeMillis = currentTimeMillis
            true
        } else {
            false
        }
    }
}

@Throws(InterruptedIOException::class)
private fun FileJob.showActionDialog(
    title: CharSequence,
    message: CharSequence,
    readOnlyFileStore: PosixFileStore?,
    showAll: Boolean,
    positiveButtonText: CharSequence?,
    negativeButtonText: CharSequence?,
    neutralButtonText: CharSequence?
): ActionResult =
    try {
        runBlocking<ActionResult> {
            suspendCoroutine { continuation ->
                BackgroundActivityStarter.startActivity(
                    FileJobActionDialogActivity::class.createIntent().putArgs(
                        FileJobActionDialogFragment.Args(
                            title, message, readOnlyFileStore, showAll, positiveButtonText,
                            negativeButtonText, neutralButtonText
                        ) { action, isAll ->
                            continuation.resume(ActionResult(action, isAll))
                        }
                    ), title, message, service
                )
            }
        }
    } catch (e: InterruptedException) {
        throw InterruptedIOException().apply { initCause(e) }
    }

private fun FileJob.getReadOnlyFileStore(path: Path, exception: IOException): PosixFileStore? {
    if (exception !is ReadOnlyFileSystemException || !path.isLinuxPath) {
        return null
    }
    val fileStore = try {
        path.getFileStore() as PosixFileStore
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return if (fileStore.isReadOnly) fileStore else null
}

private class ActionResult(
    val action: FileJobAction,
    val isAll: Boolean
)

@Throws(IOException::class)
private fun FileJob.showConflictDialog(
    sourceFile: FileItem,
    targetFile: FileItem,
    type: CopyMoveType
): ConflictResult =
    try {
        runBlocking<ConflictResult> {
            suspendCoroutine { continuation ->
                BackgroundActivityStarter.startActivity(
                    FileJobConflictDialogActivity::class.createIntent().putArgs(
                        FileJobConflictDialogFragment.Args(
                            sourceFile, targetFile, type
                        ) { action, name, all ->
                            continuation.resume(ConflictResult(action, name, all))
                        }
                    ), FileJobConflictDialogActivity.getTitle(sourceFile, targetFile, service),
                    FileJobConflictDialogActivity.getMessage(sourceFile, targetFile, type, service),
                    service
                )
            }
        }
    } catch (e: InterruptedException) {
        throw InterruptedIOException().apply { initCause(e) }
    }

enum class CopyMoveType {
    COPY,
    EXTRACT,
    MOVE
}

fun CopyMoveType.getResourceId(
    @AnyRes copyRes: Int,
    @AnyRes extractRes: Int,
    @AnyRes moveRes: Int
): Int =
    when (this) {
        CopyMoveType.COPY -> copyRes
        CopyMoveType.EXTRACT -> extractRes
        CopyMoveType.MOVE -> moveRes
    }

private class ConflictResult(
    val action: FileJobConflictAction,
    val name: String?,
    val isAll: Boolean
)

private class ActionAllInfo(
    var skipCopyMoveIntoItself: Boolean = false,
    var skipCopyMoveOverItself: Boolean = false,
    var merge: Boolean = false,
    var replace: Boolean = false,
    var skipMerge: Boolean = false,
    var skipReplace: Boolean = false,
    var skipCopyMoveError: Boolean = false,
    var skipDeleteError: Boolean = false,
    var skipRestoreSeLinuxContextError: Boolean = false,
    var skipSetGroupError: Boolean = false,
    var skipSetOwnerError: Boolean = false,
    var skipSetModeError: Boolean = false,
    var skipSetSeLinuxContextError: Boolean = false
)

class ArchiveFileJob(
    private val sources: List<Path>,
    private val archiveFile: Path,
    private val archiveType: String,
    private val compressorType: String?
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(sources, R.plurals.file_job_archive_scan_notification_title_format)
        val channel = archiveFile.newByteChannel(
            StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
        )
        var successful = false
        try {
            channel.use {
                ArchiveWriter(archiveType, compressorType, channel).use { writer ->
                    val transferInfo = TransferInfo(scanInfo, archiveFile)
                    for (source in sources) {
                        val target = getTargetFileName(source)
                        archiveRecursively(source, writer, target, transferInfo)
                        throwIfInterrupted()
                    }
                    successful = true
                }
            }
        } finally {
            if (!successful) {
                try {
                    archiveFile.deleteIfExists()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: UnsupportedOperationException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun archiveRecursively(
        source: Path,
        writer: ArchiveWriter,
        target: Path,
        transferInfo: TransferInfo
    ) {
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                val directoryInTarget = target.resolveForeign(source.relativize(directory))
                archive(directory, writer, directoryInTarget, archiveFile, transferInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                val fileInTarget = target.resolveForeign(source.relativize(file))
                archive(file, writer, fileInTarget, archiveFile, transferInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.archive(
    file: Path,
    writer: ArchiveWriter,
    entryName: Path,
    archiveFile: Path,
    transferInfo: TransferInfo
) {
    try {
        postArchiveNotification(transferInfo, file)
        writer.write(file, entryName, PROGRESS_INTERVAL_MILLIS) {
            transferInfo.addToTransferredSize(it)
            postArchiveNotification(transferInfo, file)
        }
        transferInfo.incrementTransferredFileCount()
        postArchiveNotification(transferInfo, file)
    } catch (e: InterruptedIOException) {
        throw e
    } catch (e: IOException) {
        e.printStackTrace()
        val result = showActionDialog(
            getString(R.string.file_job_archive_error_title_format, getFileName(file)),
            getString(
                R.string.file_job_archive_error_message_format, getFileName(archiveFile),
                e.toString()
            ),
            getReadOnlyFileStore(archiveFile, e),
            false,
            null,
            getString(android.R.string.cancel),
            null
        )
        when (result.action) {
            FileJobAction.NEGATIVE, FileJobAction.CANCELED -> throw InterruptedIOException()
            else -> throw AssertionError(result.action)
        }
    }
}

private fun FileJob.postArchiveNotification(transferInfo: TransferInfo, currentFile: Path) {
    postTransferSizeNotification(
        transferInfo, currentFile, R.string.file_job_archive_notification_title_one_format,
        R.plurals.file_job_archive_notification_title_multiple_format
    )
}

class CopyFileJob(private val sources: List<Path>, private val targetDirectory: Path) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val isExtract = sources.all { it.isArchivePath }
        val scanInfo = scan(
            sources, if (isExtract) {
                R.plurals.file_job_extract_scan_notification_title_format
            } else {
                R.plurals.file_job_copy_scan_notification_title_format
            }
        )
        val transferInfo = TransferInfo(scanInfo, targetDirectory)
        val actionAllInfo = ActionAllInfo()
        for (source in sources) {
            val target = if (source.parent == targetDirectory) {
                getTargetPathForDuplicate(source)
            } else {
                targetDirectory.resolveForeign(getTargetFileName(source))
            }
            copyRecursively(source, target, isExtract, transferInfo, actionAllInfo)
            throwIfInterrupted()
        }
    }

    @Throws(IOException::class)
    private fun copyRecursively(
        source: Path,
        target: Path,
        isExtract: Boolean,
        transferInfo: TransferInfo,
        actionAllInfo: ActionAllInfo
    ) {
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                val directoryInTarget = target.resolveForeign(source.relativize(directory))
                val copied = copy(
                    directory, directoryInTarget, isExtract, transferInfo, actionAllInfo
                )
                throwIfInterrupted()
                return if (copied) FileVisitResult.CONTINUE else FileVisitResult.SKIP_SUBTREE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                val fileInTarget = target.resolveForeign(source.relativize(file))
                copy(file, fileInTarget, isExtract, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }
        })
    }

    private fun getTargetPathForDuplicate(source: Path): Path {
        source.asByteStringListPath()
        val sourceFileName = source.fileNameByteString!!
        // We do want to follow symbolic links here.
        val countEndIndex = if (source.isDirectory()) {
            sourceFileName.length
        } else {
            sourceFileName.asFileName().baseName.length
        }
        val countInfo = getDuplicateCountInfo(sourceFileName, countEndIndex)
        var i = countInfo.count + 1
        while (i > 0) {
            val targetFileName = setDuplicateCount(sourceFileName, countInfo, i)
            val target = source.resolveSibling(targetFileName)
            if (!target.exists(LinkOption.NOFOLLOW_LINKS)) {
                return target
            }
            ++i
        }
        // Just leave it to conflict handling logic.
        return source
    }

    private fun getDuplicateCountInfo(fileName: ByteString, countEnd: Int): DuplicateCountInfo {
        while (true) {
            // /(?<=.) \(\d+\)$/
            var index = countEnd - 1
            // \)
            if (index < 0 || fileName[index] != ')'.code.toByte()) {
                break
            }
            --index
            // \d+
            val digitsEndInclusive = index
            while (index >= 0) {
                val b = fileName[index]
                if (b < '0'.code.toByte() || b > '9'.code.toByte()) {
                    break
                }
                --index
            }
            if (index == digitsEndInclusive) {
                break
            }
            val countString = fileName.substring(index + 1, digitsEndInclusive + 1).toString()
            val count = try {
                countString.toInt()
            } catch (e: NumberFormatException) {
                break
            }
            // \(
            if (index < 0 || fileName[index] != '('.code.toByte()) {
                break
            }
            --index
            //
            if (index < 0 || fileName[index] != ' '.code.toByte()) {
                break
            }
            // (?<=.)
            if (index == 0) {
                break
            }
            return DuplicateCountInfo(index, countEnd, count)
        }
        return DuplicateCountInfo(countEnd, countEnd, 0)
    }

    private fun setDuplicateCount(
        fileName: ByteString,
        countInfo: DuplicateCountInfo,
        count: Int
    ): ByteString {
        return ByteStringBuilder(fileName.substring(0, countInfo.countStart))
            .append(" ($count)".toByteString())
            .append(fileName.substring(countInfo.countEnd))
            .toByteString()
    }

    private class DuplicateCountInfo(val countStart: Int, val countEnd: Int, val count: Int)
}

@Throws(IOException::class)
private fun FileJob.copy(
    source: Path,
    target: Path,
    isExtract: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
): Boolean =
    copyOrMove(
        source, target, if (isExtract) CopyMoveType.EXTRACT else CopyMoveType.COPY, true, false,
        transferInfo, actionAllInfo
    )

class CreateFileJob(private val path: Path, private val createDirectory: Boolean) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        create(path, createDirectory)
    }
}

@Throws(IOException::class)
private fun FileJob.create(path: Path, createDirectory: Boolean) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            if (createDirectory) {
                path.createDirectory()
            } else {
                path.createFile()
            }
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            val result = showActionDialog(
                getString(R.string.file_job_create_error_title),
                getString(
                    R.string.file_job_create_error_message_format, getFileName(path), e.toString()
                ),
                getReadOnlyFileStore(path, e),
                false,
                getString(R.string.retry),
                getString(android.R.string.cancel),
                null
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE, FileJobAction.CANCELED -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

class DeleteFileJob(private val paths: List<Path>) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(paths, R.plurals.file_job_delete_scan_notification_title_format)
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        for (path in paths) {
            deleteRecursively(path, transferInfo, actionAllInfo)
            throwIfInterrupted()
        }
    }

    @Throws(IOException::class)
    private fun deleteRecursively(
        path: Path,
        transferInfo: TransferInfo,
        actionAllInfo: ActionAllInfo
    ) {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                delete(file, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                if (exception != null) {
                    throw exception
                }
                delete(directory, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.delete(path: Path, transferInfo: TransferInfo?, actionAllInfo: ActionAllInfo) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            path.delete()
            if (transferInfo != null) {
                transferInfo.incrementTransferredFileCount()
                postDeleteNotification(transferInfo, path)
            }
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipDeleteError) {
                if (transferInfo != null) {
                    transferInfo.skipFileIgnoringSize()
                    postDeleteNotification(transferInfo, path)
                }
                return
            }
            val result = showActionDialog(
                getString(R.string.file_job_delete_error_title),
                getString(
                    R.string.file_job_delete_error_message_format, getFileName(path), e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipDeleteError = true
                    }
                    if (transferInfo != null) {
                        transferInfo.skipFileIgnoringSize()
                        postDeleteNotification(transferInfo, path)
                    }
                    return
                }
                FileJobAction.CANCELED -> {
                    if (transferInfo != null) {
                        transferInfo.skipFileIgnoringSize()
                        postDeleteNotification(transferInfo, path)
                    }
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postDeleteNotification(transferInfo: TransferInfo, currentPath: Path) {
    postTransferCountNotification(
        transferInfo, currentPath, R.string.file_job_delete_notification_title_one_format,
        R.plurals.file_job_delete_notification_title_multiple_format
    )
}

class MoveFileJob(private val sources: List<Path>, private val targetDirectory: Path) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val sourcesToMove = mutableListOf<Path>()
        for (source in sources) {
            val target = targetDirectory.resolveForeign(source.fileName)
            try {
                moveAtomically(source, target)
            } catch (e: InterruptedIOException) {
                throw e
            } catch (e: IOException) {
                sourcesToMove.add(source)
            }
            throwIfInterrupted()
        }
        val scanInfo = scan(sourcesToMove, R.plurals.file_job_move_scan_notification_title_format)
        val transferInfo = TransferInfo(scanInfo, targetDirectory)
        val actionAllInfo = ActionAllInfo()
        for (source in sourcesToMove) {
            val target = targetDirectory.resolveForeign(source.fileName)
            moveRecursively(source, target, transferInfo, actionAllInfo)
            throwIfInterrupted()
        }
    }

    @Throws(IOException::class)
    private fun moveRecursively(
        source: Path,
        target: Path,
        transferInfo: TransferInfo,
        actionAllInfo: ActionAllInfo
    ) {
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                val directoryInTarget = target.resolveForeign(source.relativize(directory))
                try {
                    moveAtomically(directory, directoryInTarget)
                    throwIfInterrupted()
                    return FileVisitResult.SKIP_SUBTREE
                } catch (e: InterruptedIOException) {
                    throw e
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val copied = copyForMove(directory, directoryInTarget, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return if (copied) FileVisitResult.CONTINUE else FileVisitResult.SKIP_SUBTREE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                val fileInTarget = target.resolveForeign(source.relativize(file))
                try {
                    moveAtomically(file, fileInTarget)
                    throwIfInterrupted()
                    return FileVisitResult.CONTINUE
                } catch (e: InterruptedIOException) {
                    throw e
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                moveByCopy(file, fileInTarget, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult? {
                if (exception != null) {
                    throw exception
                }
                delete(directory, null, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.copyForMove(
    source: Path,
    target: Path,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
): Boolean = copyOrMove(source, target, CopyMoveType.MOVE, true, true, transferInfo, actionAllInfo)

@Throws(IOException::class)
private fun FileJob.moveAtomically(source: Path, target: Path) {
    source.moveTo(target, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.ATOMIC_MOVE)
}

@Throws(IOException::class)
private fun FileJob.moveByCopy(
    source: Path,
    target: Path,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
): Boolean =
    copyOrMove(source, target, CopyMoveType.MOVE, false, true, transferInfo, actionAllInfo)

// @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c copy_move_file
@Throws(IOException::class)
private fun FileJob.copyOrMove(
    source: Path,
    target: Path,
    type: CopyMoveType,
    useCopy: Boolean,
    copyAttributes: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
): Boolean {
    val targetParent = target.parent
    if (targetParent.startsWith(source)) {
        // Don't allow copy/move into the source itself.
        if (actionAllInfo.skipCopyMoveIntoItself) {
            transferInfo.skipFile(source)
            postCopyMoveNotification(transferInfo, source, type)
            return false
        }
        val result = showActionDialog(
            getString(
                type.getResourceId(
                    R.string.file_job_cannot_copy_into_itself_title,
                    R.string.file_job_cannot_extract_into_itself_title,
                    R.string.file_job_cannot_move_into_itself_title
                )
            ),
            getString(R.string.file_job_cannot_copy_move_into_itself_message),
            null,
            true,
            getString(R.string.skip),
            getString(android.R.string.cancel),
            null
        )
        return when (result.action) {
            FileJobAction.POSITIVE -> {
                if (result.isAll) {
                    actionAllInfo.skipCopyMoveIntoItself = true
                }
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                false
            }
            FileJobAction.CANCELED -> {
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                false
            }
            FileJobAction.NEGATIVE -> throw InterruptedIOException()
            else -> throw AssertionError(result.action)
        }
    }
    if (source.startsWith(target)) {
        // Don't allow copy/move over the source itself or its ancestors.
        if (actionAllInfo.skipCopyMoveOverItself) {
            transferInfo.skipFile(source)
            postCopyMoveNotification(transferInfo, source, type)
            return false
        }
        val result = showActionDialog(
            getString(
                type.getResourceId(
                    R.string.file_job_cannot_copy_over_itself_title,
                    R.string.file_job_cannot_extract_over_itself_title,
                    R.string.file_job_cannot_move_over_itself_title
                )
            ),
            getString(R.string.file_job_cannot_copy_move_over_itself_message),
            null,
            true,
            getString(R.string.skip),
            getString(android.R.string.cancel),
            null
        )
        return when (result.action) {
            FileJobAction.POSITIVE -> {
                if (result.isAll) {
                    actionAllInfo.skipCopyMoveOverItself = true
                }
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                false
            }
            FileJobAction.CANCELED -> {
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                false
            }
            FileJobAction.NEGATIVE -> throw InterruptedIOException()
            else -> throw AssertionError(result.action)
        }
    }
    var target = target
    var replaceExisting = false
    var retry: Boolean
    loop@ do {
        retry = false
        val options = mutableListOf<CopyOption>().apply {
            this += LinkOption.NOFOLLOW_LINKS
            if (copyAttributes) {
                this += StandardCopyOption.COPY_ATTRIBUTES
            }
            if (replaceExisting) {
                this += StandardCopyOption.REPLACE_EXISTING
            }
            this += ProgressCopyOption(PROGRESS_INTERVAL_MILLIS) {
                transferInfo.addToTransferredSize(it)
                postCopyMoveNotification(transferInfo, source, type)
            }
        }.toTypedArray()
        try {
            postCopyMoveNotification(transferInfo, source, type)
            if (useCopy) {
                source.copyTo(target, *options)
            } else {
                source.moveTo(target, *options)
            }
            transferInfo.incrementTransferredFileCount()
            postCopyMoveNotification(transferInfo, source, type)
        } catch (e: FileAlreadyExistsException) {
            val sourceFile = source.loadFileItem()
            val targetFile = target.loadFileItem()
            val sourceIsDirectory = sourceFile.attributesNoFollowLinks.isDirectory
            val targetIsDirectory = targetFile.attributesNoFollowLinks.isDirectory
            if (!sourceIsDirectory && targetIsDirectory) {
                // TODO: Don't allow replace directory with file.
                throw e
            }
            val isMerge = sourceIsDirectory && targetIsDirectory
            if (isMerge && actionAllInfo.merge) {
                transferInfo.addTransferredFile(target)
                postCopyMoveNotification(transferInfo, source, type)
                return true
            } else if (!isMerge && actionAllInfo.replace) {
                replaceExisting = true
                retry = true
                continue
            } else if ((isMerge && actionAllInfo.skipMerge)
                || (!isMerge && actionAllInfo.skipReplace)) {
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                return false
            }
            val result = showConflictDialog(sourceFile, targetFile, type)
            return when (result.action) {
                FileJobConflictAction.MERGE_OR_REPLACE -> {
                    if (result.isAll) {
                        if (isMerge) {
                            actionAllInfo.merge = true
                        } else {
                            actionAllInfo.replace = true
                        }
                    }
                    if (isMerge) {
                        transferInfo.addTransferredFile(target)
                        postCopyMoveNotification(transferInfo, source, type)
                        true
                    } else {
                        replaceExisting = true
                        retry = true
                        continue@loop
                    }
                }
                FileJobConflictAction.RENAME -> {
                    target = target.resolveSibling(result.name)
                    retry = true
                    continue@loop
                }
                FileJobConflictAction.SKIP -> {
                    if (result.isAll) {
                        if (isMerge) {
                            actionAllInfo.skipMerge = true
                        } else {
                            actionAllInfo.skipReplace = true
                        }
                    }
                    transferInfo.skipFile(source)
                    postCopyMoveNotification(transferInfo, source, type)
                    false
                }
                FileJobConflictAction.CANCELED -> {
                    transferInfo.skipFile(source)
                    postCopyMoveNotification(transferInfo, source, type)
                    false
                }
                FileJobConflictAction.CANCEL -> throw InterruptedIOException()
            }
        } catch (e: InvalidFileNameException) {
            // TODO: Prompt invalid name.
            if (false) {
                retry = true
                continue
            }
            throw e
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipCopyMoveError) {
                transferInfo.skipFile(source)
                postCopyMoveNotification(transferInfo, source, type)
                return false
            }
            val result = showActionDialog(
                getString(
                    type.getResourceId(
                        R.string.file_job_copy_error_title_format,
                        R.string.file_job_extract_error_title_format,
                        R.string.file_job_move_error_title_format
                    ), getFileName(source)
                ),
                getString(
                    type.getResourceId(
                        R.string.file_job_copy_error_message_format,
                        R.string.file_job_extract_error_message_format,
                        R.string.file_job_move_error_message_format
                    ), getFileName(targetParent), e.toString()
                ),
                getReadOnlyFileStore(target, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            return when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipCopyMoveError = true
                    }
                    transferInfo.skipFile(source)
                    postCopyMoveNotification(transferInfo, source, type)
                    false
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFile(source)
                    postCopyMoveNotification(transferInfo, source, type)
                    false
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
            }
        }
    } while (retry)
    return true
}

private fun FileJob.postCopyMoveNotification(
    transferInfo: TransferInfo,
    currentSource: Path,
    type: CopyMoveType
) {
    postTransferSizeNotification(
        transferInfo, currentSource, type.getResourceId(
            R.string.file_job_copy_notification_title_one_format,
            R.string.file_job_extract_notification_title_one_format,
            R.string.file_job_move_notification_title_one_format
        ), type.getResourceId(
            R.plurals.file_job_copy_notification_title_multiple_format,
            R.plurals.file_job_extract_notification_title_multiple_format,
            R.plurals.file_job_move_notification_title_multiple_format
        )
    )
}

class InstallApkJob(private val file: Path) : FileJob() {
    override fun run() {
        open(
            file, R.string.file_install_apk_from_background_title_format,
            R.string.file_install_apk_from_background_text
        ) { file ->
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                file.fileProviderUri
            } else {
                // PackageInstaller only supports file URI before N.
                Uri.fromFile(file.toFile())
            }
            uri.createInstallPackageIntent()
        }
    }
}

class OpenFileJob(
    private val file: Path,
    private val mimeType: MimeType,
    private val withChooser: Boolean
) : FileJob() {
    override fun run() {
        open(
            file, R.string.file_open_from_background_title_format,
            R.string.file_open_from_background_text
        ) { file ->
            file.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { extraPath = file }
                .let {
                    if (withChooser) {
                        it.withChooser(
                            OpenFileAsDialogActivity::class.createIntent()
                                .putArgs(OpenFileAsDialogFragment.Args(file))
                        )
                    } else {
                        it
                    }
                }
        }
    }
}

private val FileJob.cacheDirectory: File
    get() =
        service.externalCacheDir?.takeIf {
            Environment.getExternalStorageState(it) == Environment.MEDIA_MOUNTED
        } ?: service.cacheDir

@Throws(IOException::class)
private fun FileJob.open(
    file: Path,
    @StringRes notificationTitleFormatRes: Int,
    @StringRes notificationTextRes: Int,
    intentCreator: (Path) -> Intent
) {
    val isExtract = file.isArchivePath
    val scanInfo = scan(
        file, if (isExtract) {
            R.plurals.file_job_extract_scan_notification_title_format
        } else {
            R.plurals.file_job_copy_scan_notification_title_format
        }
    )
    val cacheDirectory = Paths.get(cacheDirectory.path, "open_cache")
    cacheDirectory.createDirectories()
    val targetFileName = getTargetFileName(file)
    val targetFile = cacheDirectory.resolveForeign(targetFileName)
    val transferInfo = TransferInfo(scanInfo, cacheDirectory)
    val actionAllInfo = ActionAllInfo(replace = true)
    copy(file, targetFile, isExtract, transferInfo, actionAllInfo)
    BackgroundActivityStarter.startActivity(
        intentCreator(targetFile), getString(notificationTitleFormatRes, targetFileName),
        getString(notificationTextRes), service
    )
}

class RenameFileJob(private val path: Path, private val newName: String) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val newPath = path.resolveSibling(newName)
        rename(path, newPath)
    }
}

@Throws(IOException::class)
private fun FileJob.rename(path: Path, newPath: Path) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            moveAtomically(path, newPath)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            val result = showActionDialog(
                getString(R.string.file_job_rename_error_title_format, getFileName(path)),
                getString(
                    R.string.file_job_rename_error_message_format, getFileName(newPath),
                    e.toString()
                ),
                getReadOnlyFileStore(path, e),
                false,
                getString(R.string.retry),
                getString(android.R.string.cancel),
                null
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE, FileJobAction.CANCELED -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

class RestoreFileSeLinuxContextJob(
    private val path: Path,
    private val recursive: Boolean
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(
            path, recursive,
            R.plurals.file_job_restore_selinux_context_scan_notification_title_format
        )
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        walkFileTreeForSettingAttributes(path, recursive, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult = visitFile(directory, attributes)

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                restoreSeLinuxContext(file, !attributes.isSymbolicLink, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.postVisitDirectory(directory, exception)
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.restoreSeLinuxContext(
    path: Path,
    followLinks: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            val options = if (followLinks) arrayOf() else arrayOf(LinkOption.NOFOLLOW_LINKS)
            path.restoreSeLinuxContext(*options)
            transferInfo.incrementTransferredFileCount()
            postRestoreSeLinuxContextNotification(transferInfo, path)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipRestoreSeLinuxContextError) {
                transferInfo.skipFileIgnoringSize()
                postRestoreSeLinuxContextNotification(transferInfo, path)
                return
            }
            val result = showActionDialog(
                getString(R.string.file_job_restore_selinux_context_error_title),
                getString(
                    R.string.file_job_restore_selinux_context_error_message_format,
                    getFileName(path), e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipRestoreSeLinuxContextError = true
                    }
                    transferInfo.skipFileIgnoringSize()
                    postRestoreSeLinuxContextNotification(transferInfo, path)
                    return
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFileIgnoringSize()
                    postRestoreSeLinuxContextNotification(transferInfo, path)
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postRestoreSeLinuxContextNotification(
    transferInfo: TransferInfo,
    currentPath: Path
) {
    postTransferCountNotification(
        transferInfo, currentPath,
        R.string.file_job_restore_selinux_context_notification_title_one_format,
        R.plurals.file_job_restore_selinux_context_notification_title_multiple_format
    )
}

class SetFileGroupJob(
    private val path: Path,
    private val group: PosixGroup,
    private val recursive: Boolean
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(
            path, recursive, R.plurals.file_job_set_group_scan_notification_title_format
        )
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        walkFileTreeForSettingAttributes(path, recursive, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult = visitFile(directory, attributes)

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                setGroup(file, group, !attributes.isSymbolicLink, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.postVisitDirectory(directory, exception)
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.setGroup(
    path: Path,
    group: PosixGroup,
    followLinks: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            val options = if (followLinks) arrayOf() else arrayOf(LinkOption.NOFOLLOW_LINKS)
            path.setGroup(group, *options)
            transferInfo.incrementTransferredFileCount()
            postSetGroupNotification(transferInfo, path)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipSetGroupError) {
                transferInfo.skipFileIgnoringSize()
                postSetGroupNotification(transferInfo, path)
                return
            }
            val result = showActionDialog(
                getString(R.string.file_job_set_group_error_title_format, getFileName(path)),
                getString(
                    R.string.file_job_set_group_error_message_format, getPrincipalName(group),
                    e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipSetGroupError = true
                    }
                    transferInfo.skipFileIgnoringSize()
                    postSetGroupNotification(transferInfo, path)
                    return
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFileIgnoringSize()
                    postSetGroupNotification(transferInfo, path)
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postSetGroupNotification(transferInfo: TransferInfo, currentPath: Path) {
    postTransferCountNotification(
        transferInfo, currentPath, R.string.file_job_set_group_notification_title_one_format,
        R.plurals.file_job_set_group_notification_title_multiple_format
    )
}

class SetFileModeJob(
    private val path: Path,
    private val mode: Set<PosixFileModeBit>,
    private val recursive: Boolean,
    private val uppercaseX: Boolean
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(
            path, recursive, R.plurals.file_job_set_mode_scan_notification_title_format
        )
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        walkFileTreeForSettingAttributes(path, recursive, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult = visitFile(directory, attributes)

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                if (attributes.isSymbolicLink) {
                    // We cannot set mode on symbolic links.
                    transferInfo.skipFileIgnoringSize()
                    return FileVisitResult.CONTINUE
                }
                // The file may actually be a directory if we are not entering it.
                val mode = if (!attributes.isDirectory) getFileMode(file) else mode
                setMode(file, mode, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.postVisitDirectory(directory, exception)
            }
        })
    }

    @Throws(IOException::class)
    private fun getFileMode(file: Path): Set<PosixFileModeBit> {
        if (file == path || !uppercaseX) {
            return mode
        }
        val mode = mode.toEnumSet()
        val currentMode = file.getMode(LinkOption.NOFOLLOW_LINKS)!!
        if (PosixFileModeBit.OWNER_EXECUTE !in currentMode) {
            mode -= PosixFileModeBit.OWNER_EXECUTE
        }
        if (PosixFileModeBit.GROUP_EXECUTE !in currentMode) {
            mode -= PosixFileModeBit.GROUP_EXECUTE
        }
        if (PosixFileModeBit.OTHERS_EXECUTE !in currentMode) {
            mode -= PosixFileModeBit.OTHERS_EXECUTE
        }
        return mode
    }
}

@Throws(IOException::class)
private fun FileJob.setMode(
    path: Path,
    mode: Set<PosixFileModeBit>,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            // This will always follow symbolic links.
            path.setMode(mode)
            transferInfo.incrementTransferredFileCount()
            postSetModeNotification(transferInfo, path)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipSetModeError) {
                transferInfo.skipFileIgnoringSize()
                postSetModeNotification(transferInfo, path)
                return
            }
            val result = showActionDialog(
                getString(R.string.file_job_set_mode_error_title_format, getFileName(path)),
                getString(
                    R.string.file_job_set_mode_error_message_format, mode.toModeString(),
                    e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipSetModeError = true
                    }
                    transferInfo.skipFileIgnoringSize()
                    postSetModeNotification(transferInfo, path)
                    return
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFileIgnoringSize()
                    postSetModeNotification(transferInfo, path)
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postSetModeNotification(transferInfo: TransferInfo, currentPath: Path) {
    postTransferCountNotification(
        transferInfo, currentPath, R.string.file_job_set_mode_notification_title_one_format,
        R.plurals.file_job_set_mode_notification_title_multiple_format
    )
}

class SetFileOwnerJob(
    private val path: Path,
    private val owner: PosixUser,
    private val recursive: Boolean
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(
            path, recursive, R.plurals.file_job_set_owner_scan_notification_title_format
        )
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        walkFileTreeForSettingAttributes(path, recursive, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult = visitFile(directory, attributes)

            @Throws(IOException::class)
            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                setOwner(file, owner, !attributes.isSymbolicLink, transferInfo, actionAllInfo)
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.postVisitDirectory(directory, exception)
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.setOwner(
    path: Path,
    owner: PosixUser,
    followLinks: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            val options = if (followLinks) arrayOf() else arrayOf(LinkOption.NOFOLLOW_LINKS)
            path.setOwner(owner, *options)
            transferInfo.incrementTransferredFileCount()
            postSetOwnerNotification(transferInfo, path)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipSetOwnerError) {
                transferInfo.skipFileIgnoringSize()
                postSetOwnerNotification(transferInfo, path)
                return
            }
            val result = showActionDialog(
                getString(R.string.file_job_set_owner_error_title_format, getFileName(path)),
                getString(
                    R.string.file_job_set_owner_error_message_format, getPrincipalName(owner),
                    e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipSetOwnerError = true
                    }
                    transferInfo.skipFileIgnoringSize()
                    postSetOwnerNotification(transferInfo, path)
                    return
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFileIgnoringSize()
                    postSetOwnerNotification(transferInfo, path)
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postSetOwnerNotification(transferInfo: TransferInfo, currentPath: Path) {
    postTransferCountNotification(
        transferInfo, currentPath, R.string.file_job_set_owner_notification_title_one_format,
        R.plurals.file_job_set_owner_notification_title_multiple_format
    )
}

private fun FileJob.getPrincipalName(principal: PosixPrincipal): String =
    principal.name ?: principal.id.toString()

class SetFileSeLinuxContextJob(
    private val path: Path,
    private val seLinuxContext: String,
    private val recursive: Boolean
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val scanInfo = scan(
            path, recursive, R.plurals.file_job_set_selinux_context_scan_notification_title_format
        )
        val transferInfo = TransferInfo(scanInfo, null)
        val actionAllInfo = ActionAllInfo()
        walkFileTreeForSettingAttributes(path, recursive, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(
                directory: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult = visitFile(directory, attributes)

            @Throws(IOException::class)
            override fun visitFile(
                file: Path,
                attributes: BasicFileAttributes
            ): FileVisitResult {
                setSeLinuxContext(
                    file, seLinuxContext, !attributes.isSymbolicLink, transferInfo, actionAllInfo
                )
                throwIfInterrupted()
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exception: IOException): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.visitFileFailed(file, exception)
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(
                directory: Path,
                exception: IOException?
            ): FileVisitResult {
                // TODO: Prompt retry, skip, skip-all or abort.
                return super.postVisitDirectory(directory, exception)
            }
        })
    }
}

@Throws(IOException::class)
private fun FileJob.setSeLinuxContext(
    path: Path,
    seLinuxContext: String,
    followLinks: Boolean,
    transferInfo: TransferInfo,
    actionAllInfo: ActionAllInfo
) {
    var retry: Boolean
    loop@ do {
        retry = false
        try {
            val options = if (followLinks) arrayOf() else arrayOf(LinkOption.NOFOLLOW_LINKS)
            path.setSeLinuxContext(seLinuxContext.toByteString(), *options)
            transferInfo.incrementTransferredFileCount()
            postSetSeLinuxContextNotification(transferInfo, path)
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            if (actionAllInfo.skipSetSeLinuxContextError) {
                transferInfo.skipFileIgnoringSize()
                postSetSeLinuxContextNotification(transferInfo, path)
                return
            }
            val result = showActionDialog(
                getString(
                    R.string.file_job_set_selinux_context_error_title_format, getFileName(path)
                ),
                getString(
                    R.string.file_job_set_selinux_context_error_message_format, seLinuxContext,
                    e.toString()
                ),
                getReadOnlyFileStore(path, e),
                true,
                getString(R.string.retry),
                getString(R.string.skip),
                getString(android.R.string.cancel)
            )
            when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE -> {
                    if (result.isAll) {
                        actionAllInfo.skipSetSeLinuxContextError = true
                    }
                    transferInfo.skipFileIgnoringSize()
                    postSetSeLinuxContextNotification(transferInfo, path)
                    return
                }
                FileJobAction.CANCELED -> {
                    transferInfo.skipFileIgnoringSize()
                    postSetSeLinuxContextNotification(transferInfo, path)
                    return
                }
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
                else -> throw AssertionError(result.action)
            }
        }
    } while (retry)
}

private fun FileJob.postSetSeLinuxContextNotification(
    transferInfo: TransferInfo,
    currentPath: Path
) {
    postTransferCountNotification(
        transferInfo, currentPath,
        R.string.file_job_set_selinux_context_notification_title_one_format,
        R.plurals.file_job_set_selinux_context_notification_title_multiple_format
    )
}

class WriteFileJob(
    private val file: Path,
    private val content: ByteArray,
    private val listener: ((Boolean) -> Unit)?
) : FileJob() {
    @Throws(IOException::class)
    override fun run() {
        val successful = write(file, content)
        listener?.let { mainExecutor.execute { it(successful) } }
    }
}

@Throws(IOException::class)
private fun FileJob.write(file: Path, content: ByteArray): Boolean {
    val scanInfo = ScanInfo().apply {
        incrementFileCount()
        addToSize(content.size.toLong())
    }
    var retry: Boolean
    loop@ do {
        retry = false
        val transferInfo = TransferInfo(scanInfo, file)
        try {
            file.newOutputStream().use { outputStream ->
                ByteArrayInputStream(content).copyTo(outputStream, PROGRESS_INTERVAL_MILLIS) {
                    transferInfo.addToTransferredSize(it)
                    postWriteNotification(transferInfo)
                }
                postWriteNotification(transferInfo)
            }
        } catch (e: InterruptedIOException) {
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            val result = showActionDialog(
                getString(R.string.file_job_write_error_title, getFileName(file)),
                getString(
                    R.string.file_job_write_error_message_format, getFileName(file), e.toString()
                ),
                getReadOnlyFileStore(file, e),
                false,
                getString(R.string.retry),
                getString(android.R.string.cancel),
                null
            )
            return when (result.action) {
                FileJobAction.POSITIVE -> {
                    retry = true
                    continue@loop
                }
                FileJobAction.NEGATIVE, FileJobAction.CANCELED -> false
                FileJobAction.NEUTRAL -> throw InterruptedIOException()
            }
        }
    } while (retry)
    return true
}

private fun FileJob.postWriteNotification(transferInfo: TransferInfo) {
    if (!transferInfo.shouldPostNotification()) {
        return
    }
    val target = transferInfo.target!!
    val title = getString(R.string.file_job_write_notification_title_format, getFileName(target))
    val size = transferInfo.size
    val sizeString = size.asFileSize().formatHumanReadable(service)
    val transferredSize = transferInfo.transferredSize
    val transferredSizeString = transferredSize.asFileSize().formatHumanReadable(service)
    val text = getString(
        R.string.file_job_transfer_size_notification_text_one_format, transferredSizeString,
        sizeString
    )
    val max = size.toInt()
    val progress = transferredSize.toInt()
    postNotification(title, text, null, null, max, progress, false, true)
}
