/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.show

class ShowRequestStoragePermissionRationaleDialogFragment : AppCompatDialogFragment() {
    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setMessage(R.string.storage_permission_rationale_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener.onShowRequestStoragePermissionRationaleResult(true)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                listener.onShowRequestStoragePermissionRationaleResult(false)
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        listener.onShowRequestStoragePermissionRationaleResult(false)
    }

    companion object {
        fun show(fragment: Fragment) {
            ShowRequestStoragePermissionRationaleDialogFragment().show(fragment)
        }
    }

    interface Listener {
        fun onShowRequestStoragePermissionRationaleResult(shouldRequest: Boolean)
    }
}
