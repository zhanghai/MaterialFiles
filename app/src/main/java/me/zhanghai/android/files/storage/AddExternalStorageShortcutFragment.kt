/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.file.ExternalStorageUri
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.createDocumentsUiViewDirectoryIntent
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.showToast

class AddExternalStorageShortcutFragment : Fragment() {
    private val args by args<Args>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val uri = args.uri
        val hasDocumentsUi = uri.value.createDocumentsUiViewDirectoryIntent()
            .resolveActivity(packageManager) != null
        if (hasDocumentsUi) {
            val externalStorageShortcut = ExternalStorageShortcut(null, null, uri)
            Storages.addOrReplace(externalStorageShortcut)
        } else {
            showToast(R.string.activity_not_found)
        }
        finish()
    }

    @Parcelize
    class Args(val uri: ExternalStorageUri) : ParcelableArgs
}
