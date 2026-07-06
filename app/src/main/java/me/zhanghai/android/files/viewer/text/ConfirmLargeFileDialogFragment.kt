/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.show

class ConfirmLargeFileDialogFragment : AppCompatDialogFragment() {
    private val fileSize: String
        get() = requireArguments().getString(EXTRA_FILE_SIZE, "")

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.file_viewer_editor_large_file_title)
            .setMessage(
                getString(R.string.file_viewer_editor_large_file_message_format, fileSize)
            )
            .setPositiveButton(R.string.file_viewer_editor_large_file_load) { _, _ ->
                listener.loadLargeFile()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        private const val EXTRA_FILE_SIZE = "file_size"

        fun show(fragment: Fragment, fileSize: String) {
            ConfirmLargeFileDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_FILE_SIZE, fileSize)
                }
            }.show(fragment)
        }
    }

    interface Listener {
        fun loadLargeFile()
    }
}
