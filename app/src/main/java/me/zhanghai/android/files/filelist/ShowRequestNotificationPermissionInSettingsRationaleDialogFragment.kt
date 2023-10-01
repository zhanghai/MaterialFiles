/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
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

class ShowRequestNotificationPermissionInSettingsRationaleDialogFragment : AppCompatDialogFragment() {
    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setMessage(R.string.notification_permission_rationale_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                listener.onShowRequestNotificationPermissionInSettingsRationaleResult(true)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                listener.onShowRequestNotificationPermissionInSettingsRationaleResult(false)
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        listener.onShowRequestNotificationPermissionInSettingsRationaleResult(false)
    }

    companion object {
        fun show(fragment: Fragment) {
            ShowRequestNotificationPermissionInSettingsRationaleDialogFragment().show(fragment)
        }
    }

    interface Listener {
        fun onShowRequestNotificationPermissionInSettingsRationaleResult(shouldRequest: Boolean)
    }
}
