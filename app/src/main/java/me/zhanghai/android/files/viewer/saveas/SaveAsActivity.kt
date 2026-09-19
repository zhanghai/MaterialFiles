/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.saveas

import android.os.Bundle
import android.os.Environment
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asMimeTypeOrNull
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.filelist.FileListActivity
import me.zhanghai.android.files.util.saveAsPaths
import me.zhanghai.android.files.util.showToast

class SaveAsActivity : AppActivity() {
    private val createFileLauncher =
        registerForActivityResult(FileListActivity.CreateFileContract(), ::onCreateFileResult)

    private val openDirectoryLauncher =
        registerForActivityResult(FileListActivity.OpenDirectoryContract(), ::onOpenDirectoryResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val paths = intent.saveAsPaths
        if (paths.isEmpty()) {
            showToast(R.string.save_as_error)
            finish()
            return
        }
        val initialPath =
            Paths.get(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            )
        if (paths.size == 1) {
            val mimeType = intent.type?.asMimeTypeOrNull() ?: MimeType.ANY
            val title = paths.first().fileName.toString()
            createFileLauncher.launch(Triple(mimeType, title, initialPath))
        } else {
            openDirectoryLauncher.launch(initialPath)
        }
    }

    private fun onCreateFileResult(result: Path?) {
        if (result == null) {
            finish()
            return
        }
        FileJobService.save(intent.saveAsPaths.first(), result, this)
        finish()
    }

    private fun onOpenDirectoryResult(result: Path?) {
        if (result == null) {
            finish()
            return
        }
        FileJobService.copy(intent.saveAsPaths, result, this)
        finish()
    }
}
