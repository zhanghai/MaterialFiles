/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditDocumentManagerShortcutDialogBinding
import me.zhanghai.android.files.file.asDocumentUriOrNull
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setTextWithSelection
import me.zhanghai.android.files.util.takeIfNotEmpty

class EditDocumentManagerShortcutDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditDocumentManagerShortcutDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_document_manager_shortcut_title)
            .apply {
                binding = EditDocumentManagerShortcutDialogBinding.inflate(context.layoutInflater)
                val documentManagerShortcut = args.documentManagerShortcut
                binding.nameLayout.placeholderText =
                    documentManagerShortcut.getDefaultName(binding.nameLayout.context)
                binding.uriEdit.hideTextInputLayoutErrorOnTextChange(binding.uriLayout)
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(
                        documentManagerShortcut.getName(binding.nameEdit.context)
                    )
                    binding.uriEdit.setText(documentManagerShortcut.uri.value.toString())
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

    private fun save() {
        val documentManagerShortcut = getDocumentManagerShortcutOrSetError() ?: return
        Storages.replace(documentManagerShortcut)
        finish()
    }

    private fun getDocumentManagerShortcutOrSetError(): DocumentManagerShortcut? {
        var errorEdit: TextInputEditText? = null
        val customName = binding.nameEdit.text.toString()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val uriText = binding.uriEdit.text.toString().takeIfNotEmpty()
        if (uriText == null) {
            binding.uriLayout.error =
                getString(R.string.storage_edit_document_manager_shortcut_uri_error_empty)
            if (errorEdit == null) {
                errorEdit = binding.uriEdit
            }
        }
        val uri = uriText?.let { Uri.parse(it).asDocumentUriOrNull() }
        if (uriText != null && uri == null) {
            binding.uriLayout.error =
                getString(R.string.storage_edit_document_manager_shortcut_uri_error_invalid)
            if (errorEdit == null) {
                errorEdit = binding.uriEdit
            }
        }
        if (errorEdit != null) {
            errorEdit.requestFocus()
            return null
        }
        return DocumentManagerShortcut(args.documentManagerShortcut.id, customName, uri!!)
    }

    private fun remove() {
        Storages.remove(args.documentManagerShortcut)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    @Parcelize
    class Args(val documentManagerShortcut: DocumentManagerShortcut) : ParcelableArgs
}
