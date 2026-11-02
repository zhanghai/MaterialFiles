/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditRcloneStorageDialogBinding
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setTextWithSelection

class EditRcloneStorageDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditRcloneStorageDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_rclone_title)
            .apply {
                binding = EditRcloneStorageDialogBinding.inflate(context.layoutInflater)
                val storage = args.rcloneStorage
                binding.remoteText.setText(storage.remoteName)
                binding.nameLayout.placeholderText = storage.getDefaultName(binding.nameLayout.context)
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(storage.getName(binding.nameEdit.context))
                    binding.pathEdit.setText(storage.relativePath)
                }
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
        val customName = binding.nameEdit.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val relativePath = binding.pathEdit.text.toString().trim().trim('/')
        val updatedStorage = args.rcloneStorage.copy(
            customName = customName,
            relativePath = relativePath
        )
        Storages.replace(updatedStorage)
        finish()
    }

    private fun remove() {
        Storages.remove(args.rcloneStorage)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        finish()
    }

    @Parcelize
    class Args(val rcloneStorage: RcloneStorage) : ParcelableArgs
}
