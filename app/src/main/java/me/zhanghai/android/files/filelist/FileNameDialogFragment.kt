/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.asFileNameOrNull

abstract class FileNameDialogFragment : NameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    override fun isNameValid(name: String): Boolean {
        if (!super.isNameValid(name)) {
            return false
        }
        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.file_name_error_empty)
            return false
        }
        if (name.asFileNameOrNull() == null) {
            binding.nameLayout.error = getString(R.string.file_name_error_invalid)
            return false
        }
        val listener = listener
        if (listener.hasFileWithName(name)) {
            binding.nameLayout.error = getString(R.string.file_name_error_already_exists)
            return false
        }
        return true
    }

    interface Listener : NameDialogFragment.Listener {
        fun hasFileWithName(name: String): Boolean
    }
}
