/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.compat.getDescriptionCompat
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.asDocumentTreeUriOrNull
import me.zhanghai.android.files.file.displayName
import me.zhanghai.android.files.file.storageVolume
import me.zhanghai.android.files.file.takePersistablePermission
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.startActivityForResultSafe

class AddDocumentTreeFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            startActivityForResultSafe(
                DocumentTreeUri.createOpenIntent(), REQUEST_CODE_OPEN_DOCUMENT_TREE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_OPEN_DOCUMENT_TREE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val documentTreeUri = data?.data?.asDocumentTreeUriOrNull()
                    if (documentTreeUri != null) {
                        addDocumentTree(documentTreeUri)
                    }
                }
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun addDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.takePersistablePermission()
        Storages.addOrReplace(
            DocumentTree(
                null, treeUri.storageVolume?.getDescriptionCompat(requireContext())
                    ?: treeUri.displayName ?: treeUri.value.toString(), treeUri
            )
        )
    }

    companion object {
        private const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 1
    }
}
