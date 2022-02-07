/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.show

class ShowRequestStoragePermissionInSettingsRationaleDialogFragment : AppCompatDialogFragment() {
    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setMessage(R.string.storage_permission_rationale_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                listener.requestStoragePermissionInSettings()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        fun show(fragment: Fragment) {
            ShowRequestStoragePermissionInSettingsRationaleDialogFragment().show(fragment)
        }
    }

    interface Listener {
        fun requestStoragePermissionInSettings()
    }
}
