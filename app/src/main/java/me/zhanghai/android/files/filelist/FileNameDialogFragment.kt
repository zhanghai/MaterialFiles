/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.FileNameDialogBinding
import me.zhanghai.android.files.databinding.FileNameDialogNameIncludeBinding
import me.zhanghai.android.files.util.asFileNameOrNull
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setOnEditorConfirmActionListener

abstract class FileNameDialogFragment : AppCompatDialogFragment() {
    private lateinit var _binding: Binding
    protected open val binding: Binding
        get() = _binding

    protected open val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(titleRes)
            .apply {
                _binding = onInflateBinding(context.layoutInflater)
                binding.nameEdit.hideTextInputLayoutErrorOnTextChange(binding.nameLayout)
                binding.nameEdit.setOnEditorConfirmActionListener { onOk() }
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                // Override the listener here so that we have control over when to close the dialog.
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { onOk() }
                }
            }

    @get:StringRes
    protected abstract val titleRes: Int

    protected open fun onInflateBinding(inflater: LayoutInflater): Binding =
        Binding.inflate(inflater)

    private fun onOk() {
        val name = name
        if (isNameUnchanged(name)) {
            dismiss()
            return
        }
        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.file_name_error_empty)
            return
        }
        if (name.asFileNameOrNull() == null) {
            binding.nameLayout.error = getString(R.string.file_name_error_invalid)
            return
        }
        val listener = listener
        if (listener.hasFileWithName(name)) {
            binding.nameLayout.error = getString(R.string.file_name_error_already_exists)
            return
        }
        onOk(name)
        dismiss()
    }

    protected open val name: String
        get() = binding.nameEdit.text.toString().trim()

    protected open fun isNameUnchanged(name: String): Boolean = false

    protected abstract fun onOk(name: String)

    protected open class Binding protected constructor(
        val root: View,
        val nameLayout: TextInputLayout,
        val nameEdit: EditText
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = FileNameDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val nameBinding = FileNameDialogNameIncludeBinding.bind(bindingRoot)
                return Binding(bindingRoot, nameBinding.nameLayout, nameBinding.nameEdit)
            }
        }
    }

    interface Listener {
        fun hasFileWithName(name: String): Boolean
    }
}
