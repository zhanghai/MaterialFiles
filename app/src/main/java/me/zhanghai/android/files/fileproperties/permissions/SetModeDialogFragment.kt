/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.ListFormatterCompat
import me.zhanghai.android.files.databinding.SetModeDialogBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.getStringArray
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.show
import me.zhanghai.android.files.util.viewModels

class SetModeDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { SetModeViewModel(argsMode) } }

    private lateinit var binding: SetModeDialogBinding

    private lateinit var normalModeBitNames: Array<String>
    private lateinit var ownerAdapter: ModeBitListAdapter
    private lateinit var groupAdapter: ModeBitListAdapter
    private lateinit var othersAdapter: ModeBitListAdapter
    private lateinit var specialModeBitNames: Array<String>
    private lateinit var specialAdapter: ModeBitListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.file_properties_permissions_set_mode_title)
            .apply {
                binding = SetModeDialogBinding.inflate(context.layoutInflater)
                binding.ownerText.setOnClickListener { binding.ownerDropDown.show() }
                val isDirectory = args.file.attributes.isDirectory
                normalModeBitNames = getStringArray(
                    if (isDirectory) {
                        R.array.file_properties_permissions_set_mode_normal_mode_bits_directory
                    } else {
                        R.array.file_properties_permissions_set_mode_normal_mode_bits_file
                    }
                )
                ownerAdapter = ModeBitListAdapter(OWNER_MODE_BITS, normalModeBitNames)
                binding.ownerDropDown.setAdapter(ownerAdapter)
                binding.ownerDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.toggleModeBit(ownerAdapter.getItem(position))
                }
                binding.groupText.setOnClickListener { binding.groupDropDown.show() }
                groupAdapter = ModeBitListAdapter(GROUP_MODE_BITS, normalModeBitNames)
                binding.groupDropDown.setAdapter(groupAdapter)
                binding.groupDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.toggleModeBit(groupAdapter.getItem(position))
                }
                binding.othersText.setOnClickListener { binding.othersDropDown.show() }
                othersAdapter = ModeBitListAdapter(OTHERS_MODE_BITS, normalModeBitNames)
                binding.othersDropDown.setAdapter(othersAdapter)
                binding.othersDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.toggleModeBit(othersAdapter.getItem(position))
                }
                binding.specialText.setOnClickListener { binding.specialDropDown.show() }
                specialModeBitNames = getStringArray(
                    R.array.file_properties_permissions_set_mode_special_mode_bits
                )
                specialAdapter = ModeBitListAdapter(SPECIAL_MODE_BITS, specialModeBitNames)
                binding.specialDropDown.setAdapter(specialAdapter)
                binding.specialDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.toggleModeBit(specialAdapter.getItem(position))
                }
                binding.recursiveCheck.isVisible = isDirectory
                binding.recursiveCheck.setOnCheckedChangeListener { _, isChecked ->
                    binding.uppercaseXCheck.isEnabled = isChecked
                }
                binding.uppercaseXCheck.isVisible = isDirectory
                if (savedInstanceState == null) {
                    binding.uppercaseXCheck.isEnabled = false
                    binding.uppercaseXCheck.isChecked = true
                }
                setView(binding.root)

                viewModel.modeLiveData.observe(this@SetModeDialogFragment) { onModeChanged(it) }
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> setMode() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

    private fun onModeChanged(mode: Set<PosixFileModeBit>) {
        binding.ownerText.setText(getModeString(OWNER_MODE_BITS, normalModeBitNames))
        ownerAdapter.mode = mode
        binding.groupText.setText(getModeString(GROUP_MODE_BITS, normalModeBitNames))
        groupAdapter.mode = mode
        binding.othersText.setText(getModeString(OTHERS_MODE_BITS, normalModeBitNames))
        othersAdapter.mode = mode
        binding.specialText.setText(getModeString(SPECIAL_MODE_BITS, specialModeBitNames))
        specialAdapter.mode = mode
    }

    private fun getModeString(
        modeBits: List<PosixFileModeBit>,
        modeBitNames: Array<String>
    ): String {
        val mode = viewModel.mode
        val checkedNames = mutableListOf<String>()
        for (index in modeBits.indices) {
            if (modeBits[index] in mode) {
                checkedNames += modeBitNames[index]
            }
        }
        return if (checkedNames.isEmpty()) {
            getString(R.string.none)
        } else {
            ListFormatterCompat.format(checkedNames)
        }
    }

    private fun setMode() {
        val mode = viewModel.mode
        val recursive = binding.recursiveCheck.isChecked
        if (!recursive) {
            if (mode == argsMode) {
                return
            }
        }
        val uppercaseX = binding.uppercaseXCheck.isChecked
        FileJobService.setMode(args.file.path, mode, recursive, uppercaseX, requireContext())
    }

    private val argsMode: Set<PosixFileModeBit>
        get() {
            val attributes = args.file.attributes as PosixFileAttributes
            return attributes.mode()!!
        }

    companion object {
        private val OWNER_MODE_BITS = listOf(
            PosixFileModeBit.OWNER_READ,
            PosixFileModeBit.OWNER_WRITE,
            PosixFileModeBit.OWNER_EXECUTE
        )
        private val GROUP_MODE_BITS = listOf(
            PosixFileModeBit.GROUP_READ,
            PosixFileModeBit.GROUP_WRITE,
            PosixFileModeBit.GROUP_EXECUTE
        )
        private val OTHERS_MODE_BITS = listOf(
            PosixFileModeBit.OTHERS_READ,
            PosixFileModeBit.OTHERS_WRITE,
            PosixFileModeBit.OTHERS_EXECUTE
        )
        private val SPECIAL_MODE_BITS = listOf(
            PosixFileModeBit.SET_USER_ID,
            PosixFileModeBit.SET_GROUP_ID,
            PosixFileModeBit.STICKY
        )

        fun show(file: FileItem, fragment: Fragment) {
            SetModeDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs
}
