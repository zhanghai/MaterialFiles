/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.file.DocumentUri
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.createDocumentManagerViewDirectoryIntent
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.showToast

class AddDocumentManagerShortcutFragment : Fragment() {
    private val args by args<Args>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val uri = args.uri
        val hasDocumentManager = uri.value.createDocumentManagerViewDirectoryIntent()
            .resolveActivity(packageManager) != null
        if (hasDocumentManager) {
            val documentManagerShortcut = DocumentManagerShortcut(
                null, args.customNameRes?.let { getString(it) }, uri
            )
            Storages.addOrReplace(documentManagerShortcut)
        } else {
            showToast(R.string.activity_not_found)
        }
        finish()
    }

    @Parcelize
    class Args(
        @StringRes val customNameRes: Int?,
        val uri: DocumentUri
    ) : ParcelableArgs
}
