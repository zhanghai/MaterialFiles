/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.EditBookmarkDirectoryDialogBinding
import me.zhanghai.android.files.filelist.FileListActivity
import me.zhanghai.android.files.filelist.userFriendlyString
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

class EditBookmarkDirectoryDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var path: Path

    private lateinit var binding: EditBookmarkDirectoryDialogBinding

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        path = savedInstanceState?.getState<State>()?.path ?: args.bookmarkDirectory.path
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.navigation_edit_bookmark_directory_title)
            .apply {
                binding = EditBookmarkDirectoryDialogBinding.inflate(context.layoutInflater)
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
        binding.pathText.setText(path.userFriendlyString)
    }

    companion object {
        private const val REQUEST_CODE_PICK_DIRECTORY = 1

        fun show(bookmarkDirectory: BookmarkDirectory, fragment: Fragment) {
            EditBookmarkDirectoryDialogFragment().putArgs(Args(bookmarkDirectory)).show(fragment)
        }
    }

    @Parcelize
    class Args(val bookmarkDirectory: BookmarkDirectory) : ParcelableArgs

    @Parcelize
    private class State(var path: @WriteWith<ParcelableParceler> Path) : ParcelableState

    interface Listener {
        fun replaceBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun removeBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
    }
}
