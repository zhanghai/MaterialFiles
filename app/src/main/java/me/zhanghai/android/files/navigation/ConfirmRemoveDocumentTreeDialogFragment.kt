/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.app.Dialog
import android.os.Bundle
import android.os.storage.StorageVolume
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat
import me.zhanghai.android.files.compat.getDescriptionCompat
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.displayNameOrUri
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.show

class ConfirmRemoveDocumentTreeDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialogBuilderCompat.create(requireContext(), theme)
            .apply {
                val name = args.storageVolume?.getDescriptionCompat(context)
                    ?: args.treeUri.displayNameOrUri
                setMessage(getString(R.string.navigation_confirm_remove_document_tree_format, name))
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener.removeDocumentTree(args.treeUri)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        fun show(treeUri: DocumentTreeUri, storageVolume: StorageVolume?, fragment: Fragment) {
            ConfirmRemoveDocumentTreeDialogFragment()
                .putArgs(Args(treeUri, storageVolume))
                .show(fragment)
        }
    }

    @Parcelize
    class Args(
        val treeUri: DocumentTreeUri,
        val storageVolume: StorageVolume?
    ) : ParcelableArgs

    interface Listener {
        fun removeDocumentTree(treeUri: DocumentTreeUri)
    }
}
