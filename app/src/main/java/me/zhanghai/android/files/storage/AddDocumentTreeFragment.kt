/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.asDocumentTreeUriOrNull
import me.zhanghai.android.files.file.takePersistablePermission
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.launchSafe
import me.zhanghai.android.files.util.showToast

class AddDocumentTreeFragment : Fragment() {
    private val openDocumentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(), this::onOpenDocumentTreeResult
    )

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            // We handle ACTION_OPEN_DOCUMENT_TREE ourselves too, but a document tree from our own
            // DocumentsProvider would only wrap paths we can already access, e.g. when DocumentsUI
            // is disabled.
            val intent = openDocumentTreeLauncher.contract.createIntent(requireContext(), null)
            val hasOtherHandler = requireContext().packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .any { it.activityInfo.packageName != requireContext().packageName }
            if (!hasOtherHandler) {
                showToast(R.string.activity_not_found)
                finish()
                return
            }
            openDocumentTreeLauncher.launchSafe(null, this)
        }
    }

    private fun onOpenDocumentTreeResult(result: Uri?) {
        val treeUri = result?.asDocumentTreeUriOrNull()
        if (treeUri != null) {
            addDocumentTree(treeUri)
        }
        finish()
    }

    private fun addDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.takePersistablePermission()
        val documentTree = DocumentTree(null, null, treeUri)
        Storages.addOrReplace(documentTree)
    }
}
