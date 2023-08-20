/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditDocumentTreeDialogBinding
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setTextWithSelection

class EditDocumentTreeDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditDocumentTreeDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_document_tree_title)
            .apply {
                binding = EditDocumentTreeDialogBinding.inflate(context.layoutInflater)
                val documentTree = args.documentTree
                binding.nameLayout.placeholderText = documentTree.getDefaultName(
                    binding.nameLayout.context
                )
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(
                        documentTree.getName(binding.nameEdit.context)
                    )
                }
                binding.uriText.setText(documentTree.uri.value.toString())
                val linuxPath = documentTree.linuxPath
                binding.pathLayout.isVisible = linuxPath != null
                binding.pathText.setText(linuxPath)
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> save() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setNeutralButton(R.string.remove) { _, _ -> remove() }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }

    private fun save() {
        val customName = binding.nameEdit.text.toString()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val documentTree = args.documentTree.copy(customName = customName)
        Storages.replace(documentTree)
        finish()
    }

    private fun remove() {
        Storages.remove(args.documentTree)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    @Parcelize
    class Args(val documentTree: DocumentTree) : ParcelableArgs
}
