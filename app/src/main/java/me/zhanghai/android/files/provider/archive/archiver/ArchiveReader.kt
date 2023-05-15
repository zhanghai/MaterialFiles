/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import java8.nio.channels.SeekableByteChannel
import java8.nio.charset.StandardCharsets
import java8.nio.file.AccessMode
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotLinkException
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.toJavaSeekableByteChannel
//#ifdef NONFREE
import me.zhanghai.android.files.nonfree.RarArchiveEntry
import me.zhanghai.android.files.nonfree.RarFile
//#endif
import me.zhanghai.android.files.provider.common.DelegateForceableSeekableByteChannel
import me.zhanghai.android.files.provider.common.DelegateInputStream
import me.zhanghai.android.files.provider.common.DelegateNonForceableSeekableByteChannel
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.provider.common.checkAccess
import me.zhanghai.android.files.provider.common.newByteChannel
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.posixFileType
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.provider.root.isRunningAsRoot
import me.zhanghai.android.files.provider.root.rootContext
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Date
import kotlin.reflect.KClass
import org.apache.commons.compress.archivers.ArchiveException as ApacheArchiveException

object ArchiveReader {
    private val compressorStreamFactory = CompressorStreamFactory()
    private val archiveStreamFactory = ArchiveStreamFactory()

    @Throws(IOException::class)
    fun readEntries(
        file: Path,
        rootPath: Path
    ): Pair<Map<Path, ArchiveEntry>, Map<Path, List<Path>>> {
        val entries = mutableMapOf<Path, ArchiveEntry>()
        val rawEntries = readEntries(file)
        for (entry in rawEntries) {
            var path = rootPath.resolve(entry.name)
            // Normalize an absolute path to prevent path traversal attack.
            if (!path.isAbsolute) {
                // TODO: Will this actually happen?
                throw AssertionError("Path must be absolute: $path")
            }
            if (path.nameCount > 0) {
                path = path.normalize()
                if (path.nameCount == 0) {
                    // Don't allow a path to become the root path only after normalization.
                    continue
                }
            }
            entries.getOrPut(path) { entry }
        }
        entries.getOrPut(rootPath) { DirectoryArchiveEntry("") }
        val tree = mutableMapOf<Path, MutableList<Path>>()
        tree[rootPath] = mutableListOf()
        val paths = entries.keys.toList()
        for (path in paths) {
            var path = path
            while (true) {
                val parentPath = path.parent ?: break
                val entry = entries[path]!!
                if (entry.isDirectory) {
                    tree.getOrPut(path) { mutableListOf() }
                }
                tree.getOrPut(parentPath) { mutableListOf() }.add(path)
                if (entries.containsKey(parentPath)) {
                    break
                }
                entries[parentPath] = DirectoryArchiveEntry(parentPath.toString())
                path = parentPath
            }
        }
        return entries to tree
    }

    @Throws(IOException::class)
    private fun readEntries(file: Path): List<ArchiveEntry> {
        val compressorType: String?
        val archiveType = try {
            file.newInputStream().buffered().use { inputStream ->
                compressorType = try {
                    // inputStream must be buffered for markSupported().
                    CompressorStreamFactory.detect(inputStream)
                } catch (e: CompressorException) {
                    // Ignored.
                    null
                }
                val compressorInputStream = if (compressorType != null) {
                    compressorStreamFactory.createCompressorInputStream(compressorType, inputStream)
                        .buffered()
                } else {
                    inputStream
                }
                try {
                    // compressorInputStream must be buffered for markSupported().
                    compressorInputStream.use { detectArchiveType(it) }
                } catch (e: ApacheArchiveException) {
                    throw ArchiveException(e)
                } catch (e: CompressorException) {
                    throw ArchiveException(e)
                }
            }
        } catch (e: FileNotFoundException) {
            file.checkAccess(AccessMode.READ)
            throw NoSuchFileException(file.toString()).apply { initCause(e) }
        }
        val encoding = archiveFileNameEncoding
        if (compressorType == null) {
            when {
                archiveType == ArchiveStreamFactory.ZIP && ZipFileCompat::class.isSupported(file) ->
                    return ZipFileCompat::class.create(file, encoding).use { it.entries.toList() }
                archiveType == ArchiveStreamFactory.SEVEN_Z -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        throw IOException(UnsupportedOperationException("SevenZFile"))
                    }
                    return SevenZFile::class.create(file).use { it.entries.toList() }
                }
                //#ifdef NONFREE
                archiveType == RarFile.RAR ->
                    return RarFile.create(file, encoding).use { it.entries.toList() }
                //#endif
                // Unnecessary, but teaches lint that compressorType != null below might be false.
                else -> {}
            }
        }
        return try {
            file.newInputStream().buffered().use { inputStream ->
                val compressorInputStream = if (compressorType != null) {
                    compressorStreamFactory.createCompressorInputStream(compressorType, inputStream)
                } else {
                    inputStream
                }
                compressorInputStream.use {
                    archiveStreamFactory.createArchiveInputStream(
                        archiveType, compressorInputStream, encoding
                    ).use { archiveInputStream ->
                        val entries = mutableListOf<ArchiveEntry>()
                        while (true) {
                            val entry = archiveInputStream.nextEntry ?: break
                            entries += entry
                        }
                        entries
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            throw NoSuchFileException(file.toString()).apply { initCause(e) }
        } catch (e: ApacheArchiveException) {
            throw ArchiveException(e)
        } catch (e: CompressorException) {
            throw ArchiveException(e)
        }
    }

    private val archiveFileNameEncoding: String
        get() =
            if (isRunningAsRoot) {
                try {
                    val sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(rootContext)
                    val key = rootContext.getString(R.string.pref_key_archive_file_name_encoding)
                    val defaultValue = rootContext.getString(
                        R.string.pref_default_value_archive_file_name_encoding
                    )
                    sharedPreferences.getString(key, defaultValue)!!
                } catch (e: Exception) {
                    e.printStackTrace()
                    StandardCharsets.UTF_8.name()
                }
            } else {
                Settings.ARCHIVE_FILE_NAME_ENCODING.valueCompat
            }

    @Throws(IOException::class)
    fun newInputStream(file: Path, entry: ArchiveEntry): InputStream {
        if (entry.isDirectory) {
            throw IsDirectoryException(file.toString())
        }
        val compressorType: String?
        val archiveType = try {
            file.newInputStream().buffered().use { inputStream ->
                compressorType = try {
                    // inputStream must be buffered for markSupported().
                    CompressorStreamFactory.detect(inputStream)
                } catch (e: CompressorException) {
                    // Ignored.
                    null
                }
                val compressorInputStream = if (compressorType != null) {
                    compressorStreamFactory.createCompressorInputStream(compressorType, inputStream)
                        .buffered()
                } else {
                    inputStream
                }
                try {
                    // compressorInputStream must be buffered for markSupported().
                    compressorInputStream.use { detectArchiveType(it) }
                } catch (e: ApacheArchiveException) {
                    throw ArchiveException(e)
                } catch (e: CompressorException) {
                    throw ArchiveException(e)
                }
            }
        } catch (e: FileNotFoundException) {
            file.checkAccess(AccessMode.READ)
            throw NoSuchFileException(file.toString()).apply { initCause(e) }
        }
        val encoding = archiveFileNameEncoding
        if (compressorType == null) {
            when {
                entry is ZipArchiveEntry && ZipFileCompat::class.isSupported(file) -> {
                    var successful = false
                    var zipFile: ZipFileCompat? = null
                    var zipEntryInputStream: InputStream? = null
                    return try {
                        zipFile = ZipFileCompat::class.create(file, encoding)
                        zipEntryInputStream = zipFile.getInputStream(entry)
                            ?: throw NoSuchFileException(file.toString())
                        val inputStream = CloseableInputStream(zipEntryInputStream, zipFile)
                        successful = true
                        inputStream
                    } finally {
                        if (!successful) {
                            zipEntryInputStream?.close()
                            zipFile?.close()
                        }
                    }
                }
                entry is SevenZArchiveEntry -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        throw IOException(UnsupportedOperationException("SevenZFile"))
                    }
                    var successful = false
                    var sevenZFile: SevenZFile? = null
                    return try {
                        sevenZFile = SevenZFile::class.create(file)
                        var inputStream: InputStream? = null
                        while (true) {
                            val currentEntry = sevenZFile.nextEntry ?: break
                            if (currentEntry.name != entry.name) {
                                continue
                            }
                            inputStream = SevenZArchiveEntryInputStream(sevenZFile, currentEntry)
                            successful = true
                            break
                        }
                        inputStream ?: throw NoSuchFileException(file.toString())
                    } finally {
                        if (!successful) {
                            sevenZFile?.close()
                        }
                    }
                }
//#ifdef NONFREE
                entry is RarArchiveEntry -> {
                    var successful = false
                    var rarFile: RarFile? = null
                    return try {
                        rarFile = RarFile.create(file, encoding)
                        var inputStream: InputStream? = null
                        while (true) {
                            val currentEntry = rarFile.nextEntry ?: break
                            if (currentEntry.name != entry.name) {
                                continue
                            }
                            inputStream = rarFile.getInputStream(currentEntry)
                            successful = true
                            break
                        }
                        inputStream ?: throw NoSuchFileException(file.toString())
                    } finally {
                        if (!successful) {
                            rarFile?.close()
                        }
                    }
                }
//#endif
                // Unnecessary, but teaches lint that compressorType != null below might be false.
                else -> {}
            }
        }
        var successful = false
        var inputStream: BufferedInputStream? = null
        var compressorInputStream: InputStream? = null
        var archiveInputStream: ArchiveInputStream? = null
        return try {
            inputStream = file.newInputStream().buffered()
            compressorInputStream = if (compressorType != null) {
                compressorStreamFactory.createCompressorInputStream(compressorType, inputStream)
            } else {
                inputStream
            }
            archiveInputStream = archiveStreamFactory.createArchiveInputStream(
                archiveType, compressorInputStream, encoding
            )
            while (true) {
                val currentEntry = archiveInputStream.nextEntry ?: break
                if (currentEntry.name != entry.name) {
                    continue
                }
                successful = true
                break
            }
            if (successful) {
                archiveInputStream
            } else {
                throw NoSuchFileException(file.toString())
            }
        } catch (e: FileNotFoundException) {
            throw NoSuchFileException(file.toString()).apply { initCause(e) }
        } catch (e: ApacheArchiveException) {
            throw ArchiveException(e)
        } catch (e: CompressorException) {
            throw ArchiveException(e)
        } finally {
            if (!successful) {
                archiveInputStream?.close()
                compressorInputStream?.close()
                inputStream?.close()
            }
        }
    }

    @Throws(ApacheArchiveException::class)
    private fun detectArchiveType(inputStream: InputStream): String =
//#ifdef NONFREE
        try {
            RarFile.detect(inputStream)
        } catch (e: IOException) {
            throw ApacheArchiveException("RarFile.detect()", e)
        } ?:
//#endif
        ArchiveStreamFactory.detect(inputStream)

    private fun KClass<ZipFileCompat>.isSupported(file: Path): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || file.isLinuxPath

    private fun KClass<ZipFileCompat>.create(file: Path, encoding: String?): ZipFileCompat =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ZipFileCompat(
                CacheSizeSeekableByteChannel(file.newByteChannel()).toJavaSeekableByteChannel(),
                encoding
            )
        } else {
            ZipFileCompat(file.toFile())
        }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun KClass<SevenZFile>.create(file: Path): SevenZFile =
        SevenZFile(CacheSizeSeekableByteChannel(file.newByteChannel()).toJavaSeekableByteChannel())

    // ZipFileCompat and SevenZFile call size() repeatedly, especially ZipFile.skipBytes(), so make
    // it cached to improve performance.
    private fun CacheSizeSeekableByteChannel(channel: SeekableByteChannel): SeekableByteChannel =
        if (channel is ForceableChannel) {
            CacheSizeForceableSeekableByteChannel(channel)
        } else {
            CacheSizeNonForceableSeekableByteChannel(channel)
        }

    private class CacheSizeNonForceableSeekableByteChannel(
        channel: SeekableByteChannel
    ) : DelegateNonForceableSeekableByteChannel(channel) {
        private val size: Long by lazy { super.size() }

        override fun size(): Long = size
    }

    private class CacheSizeForceableSeekableByteChannel(
        channel: SeekableByteChannel
    ) : DelegateForceableSeekableByteChannel(channel) {
        private val size: Long by lazy { super.size() }

        override fun size(): Long = size
    }

    @Throws(IOException::class)
    fun readSymbolicLink(file: Path, entry: ArchiveEntry): String {
        if (!isSymbolicLink(entry)) {
            throw NotLinkException(file.toString())
        }
        return if (entry is TarArchiveEntry) {
            entry.linkName
        } else {
            newInputStream(file, entry).use { it.reader(StandardCharsets.UTF_8).readText() }
        }
    }

    private fun isSymbolicLink(entry: ArchiveEntry): Boolean =
        entry.posixFileType == PosixFileType.SYMBOLIC_LINK

    private class DirectoryArchiveEntry(name: String) : ArchiveEntry {
        init {
            require(!name.endsWith("/")) { "name $name should not end with a slash" }
        }

        private val name = "$name/"

        override fun getName(): String = name

        override fun getSize(): Long = 0

        override fun isDirectory(): Boolean = true

        override fun getLastModifiedDate(): Date = Date(-1)

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            other as DirectoryArchiveEntry
            return name == other.name
        }

        override fun hashCode(): Int = name.hashCode()
    }

    private class CloseableInputStream(
        inputStream: InputStream,
        private val closeable: Closeable
    ) : DelegateInputStream(inputStream) {
        @Throws(IOException::class)
        override fun close() {
            super.close()

            closeable.close()
        }
    }

    private class SevenZArchiveEntryInputStream(
        private val file: SevenZFile,
        private val entry: SevenZArchiveEntry
    ) : InputStream() {
        override fun available(): Int {
            val size = entry.size
            val read = file.statisticsForCurrentEntry
                .uncompressedCount
            val available = size - read
            return available.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }

        @Throws(IOException::class)
        override fun read(): Int = file.read()

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int = file.read(b)

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int = file.read(b, off, len)

        @Throws(IOException::class)
        override fun close() {
            file.close()
        }
    }
}
