/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcel
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import coil.clear
import coil.loadAny
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.requireViewByIdCompat
import me.zhanghai.android.files.databinding.FileJobConflictDialogViewBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatShort
import me.zhanghai.android.files.file.iconRes
import me.zhanghai.android.files.file.lastModifiedInstant
import me.zhanghai.android.files.filelist.supportsThumbnail
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getArgs
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.readParcelable
import me.zhanghai.android.files.util.setTextWithSelection
import me.zhanghai.android.files.util.shortAnimTime
import me.zhanghai.android.files.util.showSoftInput

class FileJobConflictDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: FileJobConflictDialogViewBinding

    private var isListenerNotified = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(binding.allCheck.isChecked))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sourceFile = args.sourceFile
        val targetFile = args.targetFile
        val title = getTitle(sourceFile, targetFile, requireContext())
        val message = getMessage(sourceFile, targetFile, args.type, requireContext())
        val isMerge = isMerge(sourceFile, targetFile)
        val positiveButtonRes = if (isMerge) R.string.merge else R.string.replace
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(title)
            .setMessage(message)
            .apply {
                binding = FileJobConflictDialogViewBinding.inflate(context.layoutInflater)
                binding.targetNameText.setText(
                    if (isMerge) {
                        R.string.file_job_merge_target_name
                    } else {
                        R.string.file_job_replace_target_name
                    }
                )
                bindFileItem(
                    targetFile, binding.targetIconImage, binding.targetThumbnailImage,
                    binding.targetBadgeImage, binding.targetDescriptionText
                )
                binding.sourceNameText.setText(
                    if (isMerge) {
                        R.string.file_job_merge_source_name
                    } else {
                        R.string.file_job_replace_source_name
                    }
                )
                bindFileItem(
                    sourceFile, binding.sourceIconImage, binding.sourceThumbnailImage,
                    binding.sourceBadgeImage, binding.sourceDescriptionText
                )
                binding.showNameLayout.setOnClickListener {
                    val visible = !binding.nameLayout.isVisible
                    binding.showNameArrowImage.animate()
                        .rotation(if (visible) 90f else 0f)
                        .setDuration(shortAnimTime.toLong())
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    binding.nameLayout.isVisible = visible
                    if (visible) {
                        binding.nameEdit.requestFocus()
                        binding.nameEdit.showSoftInput()
                    }
                }
                val targetFileName = targetFile.path.fileName.toString()
                binding.nameEdit.setTextWithSelection(targetFileName)
                binding.nameEdit.doAfterTextChanged {
                    val hasNewName = hasNewName()
                    binding.allCheck.isEnabled = !hasNewName
                    if (hasNewName) {
                        binding.allCheck.isChecked = false
                    }
                    val positiveButton = requireDialog()
                        .requireViewByIdCompat<Button>(android.R.id.button1)
                    positiveButton.setText(if (hasNewName) R.string.rename else positiveButtonRes)
                }
                binding.nameLayout.setEndIconOnClickListener {
                    binding.nameEdit.setTextWithSelection(targetFileName)
                }
                if (savedInstanceState != null) {
                    binding.allCheck.isChecked = savedInstanceState.getState<State>().isAllChecked
                }
            }
            .setPositiveButton(positiveButtonRes, ::onDialogButtonClick)
            .setNegativeButton(R.string.skip, ::onDialogButtonClick)
            .setNeutralButton(android.R.string.cancel, ::onDialogButtonClick)
            .create()
            .apply { setCanceledOnTouchOutside(false) }
    }

    /** @see me.zhanghai.android.files.filelist.FileListAdapter.onBindViewHolder */
    private fun bindFileItem(
        file: FileItem,
        iconImage: ImageView,
        thumbnailImage: ImageView,
        badgeImage: ImageView,
        descriptionText: TextView
    ) {
        val path = file.path
        iconImage.setImageResource(file.mimeType.iconRes)
        iconImage.isVisible = true
        thumbnailImage.clear()
        thumbnailImage.setImageDrawable(null)
        val attributes = file.attributes
        if (file.supportsThumbnail) {
            thumbnailImage.loadAny(path to attributes) {
                listener { _, _ -> iconImage.isVisible = false }
            }
        }
        val badgeIconRes = if (file.attributesNoFollowLinks.isSymbolicLink) {
            if (file.isSymbolicLinkBroken) {
                R.drawable.error_badge_icon_18dp
            } else {
                R.drawable.symbolic_link_badge_icon_18dp
            }
        } else {
            null
        }
        val hasBadge = badgeIconRes != null
        badgeImage.isVisible = hasBadge
        if (hasBadge) {
            badgeImage.setImageResource(badgeIconRes!!)
        }
        val lastModificationTime = attributes.lastModifiedInstant
            .formatShort(descriptionText.context)
        val size = attributes.fileSize.formatHumanReadable(descriptionText.context)
        val descriptionSeparator = getString(R.string.file_item_description_separator)
        descriptionText.text = listOf(lastModificationTime, size).joinToString(descriptionSeparator)
    }

    private fun onDialogButtonClick(dialog: DialogInterface, which: Int) {
        val action: FileJobConflictAction
        val name: String?
        val all: Boolean
        when (which) {
            DialogInterface.BUTTON_POSITIVE ->
                if (hasNewName()) {
                    action = FileJobConflictAction.RENAME
                    name = binding.nameEdit.text.toString()
                    all = false
                } else {
                    action = FileJobConflictAction.MERGE_OR_REPLACE
                    name = null
                    all = binding.allCheck.isChecked
                }
            DialogInterface.BUTTON_NEGATIVE -> {
                action = FileJobConflictAction.SKIP
                name = null
                all = binding.allCheck.isChecked
            }
            DialogInterface.BUTTON_NEUTRAL -> {
                action = FileJobConflictAction.CANCEL
                name = null
                all = false
            }
            else -> throw AssertionError(which)
        }
        notifyListenerOnce(action, name, all)
        finish()
    }

    private fun hasNewName(): Boolean {
        val name = binding.nameEdit.text.toString()
        if (name.isEmpty()) {
            return false
        }
        val fileName = args.targetFile.path.fileName.toString()
        return name != fileName
    }

    override fun onStart() {
        super.onStart()

        if (binding.root.parent == null) {
            val dialog = requireDialog() as AlertDialog
            val scrollView = dialog.requireViewByIdCompat<NestedScrollView>(R.id.scrollView)
            val linearLayout = scrollView.getChildAt(0) as LinearLayout
            linearLayout.addView(binding.root)
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        notifyListenerOnce(FileJobConflictAction.CANCELED, null, false)
        finish()
    }

    fun onFinish() {
        notifyListenerOnce(FileJobConflictAction.CANCELED, null, false)
    }

    private fun notifyListenerOnce(action: FileJobConflictAction, name: String?, isAll: Boolean) {
        if (isListenerNotified) {
            return
        }
        args.listener(action, name, isAll)
        isListenerNotified = true
    }

    companion object {
        fun getTitle(sourceFile: FileItem, targetFile: FileItem, context: Context): String {
            val titleRes = if (isMerge(sourceFile, targetFile)) {
                R.string.file_job_merge_title_format
            } else {
                R.string.file_job_replace_title_format
            }
            return context.getString(titleRes, targetFile.path.fileName)
        }

        fun getMessage(
            sourceFile: FileItem,
            targetFile: FileItem,
            type: CopyMoveType,
            context: Context
        ): String {
            val messageRes = if (isMerge(sourceFile, targetFile)) {
                type.getResourceId(
                    R.string.file_job_merge_copy_message_format,
                    R.string.file_job_merge_extract_message_format,
                    R.string.file_job_merge_move_message_format
                )
            } else {
                R.string.file_job_replace_message_format
            }
            return context.getString(messageRes, targetFile.path.parent.fileName)
        }

        private fun isMerge(sourceFile: FileItem, targetFile: FileItem): Boolean {
            val sourceIsDirectory = sourceFile.attributesNoFollowLinks.isDirectory
            val targetIsDirectory = targetFile.attributesNoFollowLinks.isDirectory
            return sourceIsDirectory && targetIsDirectory
        }
    }

    @Parcelize
    class Args(
        val sourceFile: FileItem,
        val targetFile: FileItem,
        val type: CopyMoveType,
        val listener: @WriteWith<ListenerParceler>()
        (FileJobConflictAction, String?, Boolean) -> Unit
    ) : ParcelableArgs {
        object ListenerParceler : Parceler<(FileJobConflictAction, String?, Boolean) -> Unit> {
            override fun create(parcel: Parcel): (FileJobConflictAction, String?, Boolean) -> Unit =
                parcel.readParcelable<RemoteCallback>()!!.let {
                    { action, name, isAll ->
                        it.sendResult(Bundle().putArgs(ListenerArgs(action, name, isAll)))
                    }
                }

            override fun ((FileJobConflictAction, String?, Boolean) -> Unit).write(
                parcel: Parcel,
                flags: Int
            ) {
                parcel.writeParcelable(
                    RemoteCallback {
                        val args = it.getArgs<ListenerArgs>()
                        this(args.action, args.name, args.isAll)
                    }, flags
                )
            }

            @Parcelize
            private class ListenerArgs(
                val action: FileJobConflictAction,
                val name: String?,
                val isAll: Boolean
            ) : ParcelableArgs
        }
    }

    @Parcelize
    private class State(val isAllChecked: Boolean) : ParcelableState
}
