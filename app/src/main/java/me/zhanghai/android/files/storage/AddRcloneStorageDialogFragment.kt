/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.AddRcloneStorageDialogBinding
import me.zhanghai.android.files.provider.rclone.client.RcloneConfigFile
import me.zhanghai.android.files.provider.rclone.daemon.RcloneDaemon
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.launchSafe
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.showToast

class AddRcloneStorageDialogFragment : AppCompatDialogFragment() {
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(), this::onOpenDocumentResult
    )

    private lateinit var binding: AddRcloneStorageDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_add_rclone_title)
            .apply {
                binding = AddRcloneStorageDialogBinding.inflate(context.layoutInflater)
                binding.chooseFileButton.setOnClickListener {
                    openDocumentLauncher.launchSafe(arrayOf("*/*"), this@AddRcloneStorageDialogFragment)
                }
                val context = requireContext()
                val existingConfigFile = RcloneDaemon.getConfigFile(context)
                if (existingConfigFile.exists() && binding.configContentEdit.text.isNullOrEmpty()) {
                    binding.configContentEdit.setText(existingConfigFile.readText())
                }
                val isAvailable = RcloneDaemon.isAvailable(context)
                if (!isAvailable) {
                    binding.statusText.isVisible = true
                    binding.statusText.setText(R.string.storage_rclone_binary_not_found)
                }
                setView(binding.root)
            }
            .setPositiveButton(R.string.storage_rclone_import) { _, _ -> importConfig() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }

    private fun onOpenDocumentResult(uri: Uri?) {
        if (uri == null) {
            return
        }
        try {
            val content = requireContext().contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            }
            if (!content.isNullOrEmpty()) {
                binding.configContentEdit.setText(content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read rclone.conf from uri $uri", e)
        }
    }

    private fun importConfig() {
        val content = binding.configContentEdit.text.toString().trim()
        if (content.isEmpty()) {
            binding.configLayout.error = getString(R.string.storage_rclone_empty_config_error)
            return
        }
        val remotes = RcloneConfigFile.parseRemotes(content)
        if (remotes.isEmpty()) {
            binding.configLayout.error = getString(R.string.storage_rclone_no_remotes_error)
            return
        }

        val context = requireContext()
        RcloneDaemon.saveConfigFile(context, content)

        for (remote in remotes) {
            val storage = RcloneStorage(null, null, remote)
            Storages.addOrReplace(storage)
        }

        try {
            RcloneDaemon.restart(context)
        } catch (e: Exception) {
            Log.w(TAG, "Could not start rclone daemon immediately", e)
        }

        showToast(getString(R.string.storage_rclone_remotes_added, remotes.joinToString(", ")))
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        finish()
    }

    companion object {
        private const val TAG = "AddRcloneStorageDialog"
    }
}
