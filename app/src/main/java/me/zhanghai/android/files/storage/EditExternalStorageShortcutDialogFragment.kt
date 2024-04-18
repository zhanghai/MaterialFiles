/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditExternalStorageShortcutDialogBinding
import me.zhanghai.android.files.file.ExternalStorageUri
import me.zhanghai.android.files.file.displayName
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setTextWithSelection
import me.zhanghai.android.files.util.takeIfNotEmpty

class EditExternalStorageShortcutDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditExternalStorageShortcutDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_external_storage_shortcut_title)
            .apply {
                binding = EditExternalStorageShortcutDialogBinding.inflate(context.layoutInflater)
                val externalStorageShortcut = args.externalStorageShortcut
                binding.rootIdEdit.hideTextInputLayoutErrorOnTextChange(binding.rootIdLayout)
                binding.rootIdEdit.doAfterTextChanged { updateNamePlaceholder() }
                binding.pathEdit.doAfterTextChanged { updateNamePlaceholder() }
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(
                        externalStorageShortcut.getName(binding.nameEdit.context)
                    )
                    val uri = externalStorageShortcut.uri
                    binding.rootIdEdit.setText(uri.rootId)
                    binding.pathEdit.setText(uri.path)
                }
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setNeutralButton(R.string.remove) { _, _ -> remove() }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                // Override the listener here so that we have control over when to close the dialog.
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { save() }
                }
            }

    private fun updateNamePlaceholder() {
        val rootId = binding.rootIdEdit.text.toString().takeIfNotEmpty()
        val path = binding.pathEdit.text.toString().dropWhile { it == '/' }
        binding.nameLayout.placeholderText = if (rootId != null) {
            ExternalStorageUri(rootId, path).displayName
        } else {
            path
        }
    }

    private fun save() {
        val externalStorageShortcut = getExternalStorageShortcutOrSetError() ?: return
        Storages.replace(externalStorageShortcut)
        finish()
    }

    private fun getExternalStorageShortcutOrSetError(): ExternalStorageShortcut? {
        var errorEdit: TextInputEditText? = null
        val customName = binding.nameEdit.text.toString()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val rootId = binding.rootIdEdit.text.toString().takeIfNotEmpty()
        if (rootId == null) {
            binding.rootIdLayout.error =
                getString(R.string.storage_edit_external_storage_shortcut_root_id_error_empty)
            if (errorEdit == null) {
                errorEdit = binding.rootIdEdit
            }
        }
        val path = binding.pathEdit.text.toString().dropWhile { it == '/' }
        val uri = rootId?.let { ExternalStorageUri(rootId, path) }
        if (errorEdit != null) {
            errorEdit.requestFocus()
            return null
        }
        return ExternalStorageShortcut(args.externalStorageShortcut.id, customName, uri!!)
    }

    private fun remove() {
        Storages.remove(args.externalStorageShortcut)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    @Parcelize
    class Args(val externalStorageShortcut: ExternalStorageShortcut) : ParcelableArgs
}
