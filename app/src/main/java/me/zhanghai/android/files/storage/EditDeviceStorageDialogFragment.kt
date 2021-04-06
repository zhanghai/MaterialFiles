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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditDeviceStorageDialogBinding
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setTextWithSelection

class EditDeviceStorageDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditDeviceStorageDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val deviceStorage = args.deviceStorage
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_device_storage_title)
            .apply {
                binding = EditDeviceStorageDialogBinding.inflate(context.layoutInflater)
                binding.nameLayout.placeholderText = deviceStorage.getDefaultName(
                    binding.nameLayout.context
                )
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(
                        deviceStorage.getName(binding.nameEdit.context)
                    )
                }
                binding.pathText.setText(deviceStorage.linuxPath)
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> save() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setNeutralButton(
                if (deviceStorage.isVisible) R.string.hide else R.string.show
            ) { _, _ -> toggleVisibility() }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
    }

    private fun save() {
        val customName = binding.nameEdit.text.toString()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val deviceStorage = args.deviceStorage.copy_(customName = customName)
        Storages.replace(deviceStorage)
        finish()
    }

    private fun toggleVisibility() {
        val deviceStorage = args.deviceStorage.let { it.copy_(isVisible = !it.isVisible) }
        Storages.replace(deviceStorage)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    @Parcelize
    class Args(val deviceStorage: DeviceStorage) : ParcelableArgs
}
