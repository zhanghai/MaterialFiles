/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.CreateArchiveDialogBinding
import me.zhanghai.android.files.databinding.NameDialogNameIncludeBinding
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.setTextWithSelection
import me.zhanghai.android.files.util.show
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.libarchive.Archive

class CreateArchiveDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (savedInstanceState == null) {
            val files = args.files
            var name: String? = null
            if (files.size == 1) {
                name = files.single().path.fileName.toString()
            } else {
                val parent = files.mapTo(mutableSetOf()) { it.path.parent }.singleOrNull()
                if (parent != null && parent.nameCount > 0) {
                    name = parent.fileName.toString()
                }
            }
            name?.let { binding.nameEdit.setTextWithSelection(it) }
        }
        binding.typeGroup.setOnCheckedChangeListener { _, _ -> updatePasswordLayoutVisibility() }
        updatePasswordLayoutVisibility()
        return dialog
    }

    @StringRes
    override val titleRes: Int = R.string.file_create_archive_title

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override val name: String
        get() {
            val extension = when (val checkedId = binding.typeGroup.checkedRadioButtonId) {
                R.id.zipRadio -> "zip"
                R.id.tarXzRadio -> "tar.xz"
                R.id.sevenZRadio -> "7z"
                else -> throw AssertionError(checkedId)
            }
            return "${super.name}.$extension"
        }

    private val isPasswordSupported: Boolean
        get() = when (val checkedId = binding.typeGroup.checkedRadioButtonId) {
            R.id.zipRadio -> true
            R.id.tarXzRadio, R.id.sevenZRadio -> false
            else -> throw AssertionError(checkedId)
        }

    private fun updatePasswordLayoutVisibility() {
        binding.passwordLayout.isGone = !isPasswordSupported
    }

    override fun onOk(name: String) {
        val (format, filter) = when (val checkedId = binding.typeGroup.checkedRadioButtonId) {
            R.id.zipRadio -> Archive.FORMAT_ZIP to Archive.FILTER_NONE
            R.id.tarXzRadio -> Archive.FORMAT_TAR to Archive.FILTER_XZ
            R.id.sevenZRadio -> Archive.FORMAT_7ZIP to Archive.FILTER_NONE
            else -> throw AssertionError(checkedId)
        }
        val password = if (isPasswordSupported) {
            binding.passwordEdit.text!!.toString().takeIfNotEmpty()
        } else {
            null
        }
        listener.archive(args.files, name, format, filter, password)
    }

    companion object {
        fun show(files: FileItemSet, fragment: Fragment) {
            CreateArchiveDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet) : ParcelableArgs

    protected class Binding private constructor(
        root: View,
        nameLayout: TextInputLayout,
        nameEdit: EditText,
        val typeGroup: RadioGroup,
        val passwordLayout: TextInputLayout,
        val passwordEdit: TextInputEditText
    ) : NameDialogFragment.Binding(root, nameLayout, nameEdit) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = CreateArchiveDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val nameBinding = NameDialogNameIncludeBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot, nameBinding.nameLayout, nameBinding.nameEdit, binding.typeGroup,
                    binding.passwordLayout, binding.passwordEdit
                )
            }
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun archive(files: FileItemSet, name: String, format: Int, filter: Int, password: String?)
    }
}
