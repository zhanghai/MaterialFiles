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
import me.zhanghai.android.files.databinding.NameDialogBinding
import me.zhanghai.android.files.databinding.NameDialogNameIncludeBinding
import me.zhanghai.android.files.util.hideTextInputLayoutErrorOnTextChange
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.setOnEditorConfirmActionListener

abstract class NameDialogFragment : AppCompatDialogFragment() {
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
                if (savedInstanceState == null) {
                    val initialName = initialName
                    if (initialName != null) {
                        binding.nameEdit.setText(initialName)
                        binding.nameEdit.setSelection(0, initialName.length)
                    }
                }
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

    protected open val initialName: String? = null

    protected open fun onInflateBinding(inflater: LayoutInflater): Binding =
        Binding.inflate(inflater)

    private fun onOk() {
        val name = name
        if (!isNameValid(name)) {
            return
        }
        onOk(name)
        dismiss()
    }

    protected open val name: String
        get() = binding.nameEdit.text.toString().trim()

    protected open fun isNameValid(name: String): Boolean {
        if (name == initialName) {
            dismiss()
            return false
        }
        return true
    }

    protected abstract fun onOk(name: String)

    protected open class Binding protected constructor(
        val root: View,
        val nameLayout: TextInputLayout,
        val nameEdit: EditText
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = NameDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val nameBinding = NameDialogNameIncludeBinding.bind(bindingRoot)
                return Binding(bindingRoot, nameBinding.nameLayout, nameBinding.nameEdit)
            }
        }
    }

    interface Listener
}
