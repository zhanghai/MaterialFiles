/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.show

/**
 * Asks the user whether the app that started the picker may access root/Shizuku-only locations.
 */
class ConfirmExternalRootAccessDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val appLabel = args.appLabel
            ?: getString(R.string.file_list_pick_root_access_unknown_app)
        val message = getString(
            R.string.file_list_pick_root_access_message_format, appLabel,
            args.displayPaths.joinToString("\n")
        )
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.file_list_pick_root_access_title)
            .setMessage(message)
            .setPositiveButton(R.string.file_list_pick_root_access_allow) { _, _ ->
                listener.allowExternalRootAccess(args.documentIds)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        fun show(
            documentIds: List<String>,
            displayPaths: List<String>,
            appLabel: String?,
            fragment: Fragment
        ) {
            ConfirmExternalRootAccessDialogFragment()
                .putArgs(Args(documentIds, displayPaths, appLabel))
                .show(fragment)
        }
    }

    @Parcelize
    class Args(
        val documentIds: List<String>,
        val displayPaths: List<String>,
        val appLabel: String?
    ) : ParcelableArgs

    interface Listener {
        /** Called with the document IDs of all picked paths once the user allows root access. */
        fun allowExternalRootAccess(documentIds: List<String>)
    }
}
