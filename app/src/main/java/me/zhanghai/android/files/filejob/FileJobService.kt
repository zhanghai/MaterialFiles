/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.MainThread
import java8.nio.file.Path
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixUser
import me.zhanghai.android.files.util.ForegroundNotificationManager
import me.zhanghai.android.files.util.removeFirst
import java.util.concurrent.Executors
import java.util.concurrent.Future

class FileJobService : Service() {
    internal lateinit var notificationManager: ForegroundNotificationManager
        private set

    private val executorService = Executors.newCachedThreadPool()

    private val runningJobs = mutableMapOf<FileJob, Future<*>>()

    override fun onCreate() {
        super.onCreate()

        notificationManager = ForegroundNotificationManager(this)
        instance = this

        while (pendingJobs.isNotEmpty()) {
            startJob(pendingJobs.removeFirst())
        }
    }

    private fun startJob(job: FileJob) {
        // Synchronize on runningJobs to prevent a job from removing itself before being added.
        synchronized(runningJobs) {
            val future = executorService.submit {
                job.runOn(this)
                synchronized(runningJobs) { runningJobs.remove(job) }
            }
            runningJobs[job] = future
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private val jobCount: Int
        get() = synchronized(runningJobs) { runningJobs.size }

    private fun cancelJob(id: Int) {
        synchronized(runningJobs) {
            runningJobs.removeFirst { it.key.id == id }?.value?.cancel(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        instance = null

        synchronized(runningJobs) {
            while (runningJobs.isNotEmpty()) {
                runningJobs.removeFirst().value.cancel(true)
            }
        }
    }

    companion object {
        private var instance: FileJobService? = null

        private val pendingJobs = mutableListOf<FileJob>()

        val runningJobCount: Int
            @MainThread
            get() = instance?.jobCount ?: 0

        @MainThread
        private fun startJob(job: FileJob, context: Context) {
            val instance = instance
            if (instance != null) {
                instance.startJob(job)
            } else {
                pendingJobs.add(job)
                context.startService(Intent(context, FileJobService::class.java))
            }
        }

        fun archive(
            sources: List<Path>,
            archiveFile: Path,
            archiveType: String,
            compressorType: String?,
            context: Context
        ) {
            startJob(ArchiveFileJob(sources, archiveFile, archiveType, compressorType), context)
        }

        fun copy(sources: List<Path>, targetDirectory: Path, context: Context) {
            startJob(CopyFileJob(sources, targetDirectory), context)
        }

        fun create(path: Path, createDirectory: Boolean, context: Context) {
            startJob(CreateFileJob(path, createDirectory), context)
        }

        fun delete(paths: List<Path>, context: Context) {
            startJob(DeleteFileJob(paths), context)
        }

        fun move(sources: List<Path>, targetDirectory: Path, context: Context) {
            startJob(MoveFileJob(sources, targetDirectory), context)
        }

        fun installApk(file: Path, context: Context) {
            startJob(InstallApkJob(file), context)
        }

        fun open(file: Path, mimeType: MimeType, withChooser: Boolean, context: Context) {
            startJob(OpenFileJob(file, mimeType, withChooser), context)
        }

        fun rename(path: Path, newName: String, context: Context) {
            startJob(RenameFileJob(path, newName), context)
        }

        fun restoreSeLinuxContext(path: Path, recursive: Boolean, context: Context) {
            startJob(RestoreFileSeLinuxContextJob(path, recursive), context)
        }

        fun setGroup(path: Path, group: PosixGroup, recursive: Boolean, context: Context) {
            startJob(SetFileGroupJob(path, group, recursive), context)
        }

        fun setMode(
            path: Path,
            mode: Set<PosixFileModeBit>,
            recursive: Boolean,
            uppercaseX: Boolean,
            context: Context
        ) {
            startJob(SetFileModeJob(path, mode, recursive, uppercaseX), context)
        }

        fun setOwner(path: Path, owner: PosixUser, recursive: Boolean, context: Context) {
            startJob(SetFileOwnerJob(path, owner, recursive), context)
        }

        fun setSeLinuxContext(
            path: Path,
            seLinuxContext: String,
            recursive: Boolean,
            context: Context
        ) {
            startJob(SetFileSeLinuxContextJob(path, seLinuxContext, recursive), context)
        }

        fun write(
            file: Path,
            content: ByteArray,
            context: Context,
            listener: ((Boolean) -> Unit)?
        ) {
            startJob(WriteFileJob(file, content, listener), context)
        }

        @MainThread
        fun cancelJob(id: Int) {
            pendingJobs.removeFirst { it.id == id }
            instance?.cancelJob(id)
        }
    }
}
