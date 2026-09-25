/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.mtp.MtpConstants
import android.os.AsyncTask
import android.provider.MediaStore
import java8.nio.file.Paths
import me.zhanghai.android.files.app.contentResolver
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.loadFileItem
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.CloseableLiveData
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Lists the most recently modified files on the device, newest first.
 *
 * The candidate files come from the media database, which indexes every kind of file on the shared
 * storage volumes (images, videos, audio, documents, packages and so on) as long as we have access
 * to it, so no directory needs to be walked for this.
 *
 * Before Android Q the media database also indexes directories, and a lot of files that are never
 * shown, so more rows are looked at than there are files to display.
 *
 * Files that are only of use to the app that made them, like temporary files, unfinished downloads,
 * caches, thumbnails and logs, are left out. Apps keep modifying such files, so they would otherwise
 * crowd out the files that people actually work with.
 */
class RecentFilesLiveData(
    private val query: String?
) : CloseableLiveData<Stateful<List<FileItem>>>() {
    private var future: Future<Unit>? = null

    init {
        loadValue()
    }

    fun loadValue() {
        future?.cancel(true)
        // The list is rebuilt from scratch and published as it is being loaded.
        value = Loading(emptyList())
        val showHiddenFiles = Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat
        future = (AsyncTask.THREAD_POOL_EXECUTOR as ExecutorService).submit<Unit> {
            try {
                val fileList = mutableListOf<FileItem>()
                for (path in queryRecentFilePaths(showHiddenFiles)) {
                    if (Thread.currentThread().isInterrupted) {
                        return@submit
                    }
                    if (fileList.size >= FILE_COUNT_MAX) {
                        break
                    }
                    if (query != null &&
                        !path.substringAfterLast('/').contains(query, ignoreCase = true)) {
                        continue
                    }
                    val fileItem = try {
                        Paths.get(path).loadFileItem()
                    } catch (e: IOException) {
                        // The media database can be out of date, so simply skip such a file.
                        continue
                    }
                    // Files that won't be shown shouldn't take up room in the list either.
                    if (fileItem.attributes.isDirectory || (fileItem.isHidden && !showHiddenFiles)) {
                        continue
                    }
                    fileList.add(fileItem)
                    if (fileList.size % LOAD_INTERVAL_SIZE == 0) {
                        postValue(Loading(fileList.sortedByLastModifiedDescending()))
                    }
                }
                postValue(Success(fileList.sortedByLastModifiedDescending()))
            } catch (e: Exception) {
                // TODO: Retrieval of previous value is racy.
                postValue(Failure(valueCompat.value, e))
            }
        }
    }

    /**
     * The media database timestamps can differ from the file system ones that we display, so sort
     * by the latter to make sure the list actually reads newest first.
     */
    private fun List<FileItem>.sortedByLastModifiedDescending(): List<FileItem> =
        sortedByDescending { it.attributes.lastModifiedTime() }

    @Suppress("DEPRECATION")
    private fun queryRecentFilePaths(showHiddenFiles: Boolean): List<String> {
        val uri = MediaStore.Files.getContentUri(VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        // Directories are indexed as files before Android Q but are never listed here, so leave
        // them out of the query instead of letting them use up the rows we look at. The column
        // isn't public API, so fall back to querying without it.
        val cursor = try {
            contentResolver.query(
                uri, projection, "$COLUMN_FORMAT IS NULL OR $COLUMN_FORMAT != ?",
                arrayOf(MtpConstants.FORMAT_ASSOCIATION.toString()), sortOrder
            )
        } catch (e: Exception) {
            e.printStackTrace()
            contentResolver.query(uri, projection, null, null, sortOrder)
        }
        val paths = linkedSetOf<String>()
        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            while (paths.size < PATH_COUNT_MAX && it.position < ROW_COUNT_MAX && it.moveToNext()) {
                val path = it.getString(dataIndex)
                if (path.isNullOrEmpty() || isJunkFile(path, showHiddenFiles)) {
                    continue
                }
                paths += path
            }
        }
        return paths.toList()
    }

    private fun isJunkFile(path: String, showHiddenFiles: Boolean): Boolean {
        val lowercasePath = path.lowercase()
        if ("/android/obb/" in lowercasePath) {
            return true
        }
        val segments = lowercasePath.split('/').filter { it.isNotEmpty() }
        val name = segments.lastOrNull() ?: return true
        val isInJunkDirectory = segments.dropLast(1).any {
            it in JUNK_DIRECTORY_NAMES || (!showHiddenFiles && it.startsWith('.'))
        }
        return isInJunkDirectory || name in JUNK_FILE_NAMES ||
            JUNK_FILE_NAME_PREFIXES.any { name.startsWith(it) } ||
            JUNK_FILE_NAME_SUFFIXES.any { name.endsWith(it) } ||
            name.substringAfterLast('.', "") in JUNK_EXTENSIONS
    }

    override fun close() {
        future?.cancel(true)
    }

    companion object {
        // MediaStore.VOLUME_EXTERNAL, which is a view of every shared storage volume since Android
        // Q and the only external volume name before that.
        private const val VOLUME_EXTERNAL = "external"

        // MediaStore.Files.FileColumns.FORMAT, which isn't public API.
        private const val COLUMN_FORMAT = "format"

        private const val FILE_COUNT_MAX = 500

        private const val PATH_COUNT_MAX = 4 * FILE_COUNT_MAX

        private const val ROW_COUNT_MAX = 20 * PATH_COUNT_MAX

        // Caches, thumbnails, temporary files, logs, trash, and fragments recovered by fsck.
        private val JUNK_DIRECTORY_NAMES = setOf(
            ".cache", ".temp", ".thumbnails", ".tmp", ".trash", ".trashes", "cache", "log", "logs",
            "lost.dir", "temp", "tmp"
        )

        private val JUNK_FILE_NAMES = setOf(".ds_store", ".nomedia", "desktop.ini", "thumbs.db")

        // Pending and trashed media, Office lock files, macOS resource forks and thumbnail caches.
        private val JUNK_FILE_NAME_PREFIXES = listOf(
            ".pending-", ".trashed-", "~$", "._", ".thumbdata"
        )

        // Editor backups and SQLite temporary files.
        private val JUNK_FILE_NAME_SUFFIXES = listOf("~", "-journal", "-shm", "-wal")

        // Temporary files, unfinished downloads, editor swap files, locks and logs.
        private val JUNK_EXTENSIONS = setOf(
            "aria2", "crdownload", "download", "lck", "lock", "log", "opdownload", "part",
            "partial", "swo", "swp", "temp", "tmp", "xlog"
        )

        private const val LOAD_INTERVAL_SIZE = 50
    }
}
