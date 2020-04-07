/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat
import me.zhanghai.android.files.databinding.EditBookmarkDirectoryDialogBinding
import me.zhanghai.android.files.databinding.EditBookmarkDirectoryDialogMd2Binding
import me.zhanghai.android.files.filelist.FileListActivity
import me.zhanghai.android.files.filelist.userFriendlyString
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.setTextWithSelection
import me.zhanghai.android.files.util.show
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.files.util.valueCompat

class EditBookmarkDirectoryDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var path: Path

    private lateinit var binding: Binding

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        path = savedInstanceState?.getState<State>()?.path ?: args.bookmarkDirectory.path
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialogBuilderCompat.create(requireContext(), theme)
            .setTitle(R.string.navigation_edit_bookmark_directory_title)
            .apply {
                binding = Binding.inflate(context.layoutInflater)
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(args.bookmarkDirectory.name)
                }
                updatePathButton()
                binding.pathText.setOnClickListener {
                    val intent = FileListActivity.createPickDirectoryIntent(path)
                    startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY)
                }
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val customName = binding.nameEdit.text.toString().takeIfNotEmpty()
                val bookmarkDirectory = args.bookmarkDirectory.copy(
                    customName = customName, path = path
                )
                listener.replaceBookmarkDirectory(bookmarkDirectory)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.remove) { _, _ ->
                listener.removeBookmarkDirectory(args.bookmarkDirectory)
            }
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(path))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PICK_DIRECTORY ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.extraPath?.let {
                        path = it
                        updatePathButton()
                    }
                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updatePathButton() {
        binding.pathText.text = path.userFriendlyString
    }

    companion object {
        private const val REQUEST_CODE_PICK_DIRECTORY = 1

        fun show(bookmarkDirectory: BookmarkDirectory, fragment: Fragment) {
            EditBookmarkDirectoryDialogFragment().putArgs(Args(bookmarkDirectory)).show(fragment)
        }
    }

    @Parcelize
    class Args(val bookmarkDirectory: BookmarkDirectory) : ParcelableArgs

    private class Binding private constructor(
        val root: View,
        val nameEdit: EditText,
        val pathText: TextView
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding =
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    val binding = EditBookmarkDirectoryDialogMd2Binding.inflate(inflater)
                    Binding(binding.root, binding.nameEdit, binding.pathText)
                } else {
                    val binding = EditBookmarkDirectoryDialogBinding.inflate(inflater)
                    Binding(binding.root, binding.nameEdit, binding.pathText)
                }
        }
    }

    @Parcelize
    private class State(var path: @WriteWith<ParcelableParceler> Path) : ParcelableState

    interface Listener {
        fun replaceBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun removeBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
    }
}
