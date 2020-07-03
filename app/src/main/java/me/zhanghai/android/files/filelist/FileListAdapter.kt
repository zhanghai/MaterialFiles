/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.clear
import coil.api.loadAny
import java8.nio.file.Path
import me.zhanghai.android.fastscroll.PopupTextProvider
import me.zhanghai.android.files.R
import me.zhanghai.android.files.coil.ignoreError
import me.zhanghai.android.files.compat.getDrawableCompat
import me.zhanghai.android.files.databinding.FileItemBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatShort
import me.zhanghai.android.files.file.iconRes
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.ui.AnimatedListAdapter
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.valueCompat
import java.util.Comparator
import java.util.Locale

class FileListAdapter(
    private val listener: Listener
) : AnimatedListAdapter<FileItem, FileListAdapter.ViewHolder>(
    CALLBACK
), PopupTextProvider {
    private var isSearching = false

    private lateinit var _comparator: Comparator<FileItem>
    var comparator: Comparator<FileItem>
        get() = _comparator
        set(value) {
            _comparator = value
            if (!isSearching) {
                super.replace(list.sortedWith(value), true)
                rebuildFilePositionMap()
            }
        }

    var pickOptions: PickOptions? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_STATE_CHANGED)
        }

    private val selectedFiles = fileItemSetOf()

    private val filePositionMap = mutableMapOf<Path, Int>()

    fun replaceSelectedFiles(files: FileItemSet) {
        val changedFiles = fileItemSetOf()
        val iterator = selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file !in files) {
                iterator.remove()
                changedFiles.add(file)
            }
        }
        for (file in files) {
            if (file !in selectedFiles) {
                selectedFiles.add(file)
                changedFiles.add(file)
            }
        }
        for (file in changedFiles) {
            val position = filePositionMap[file.path]
            position?.let { notifyItemChanged(it, PAYLOAD_STATE_CHANGED) }
        }
    }

    private fun selectFile(file: FileItem) {
        if (!isFileSelectable(file)) {
            return
        }
        val pickOptions = pickOptions
        if (pickOptions != null && !pickOptions.allowMultiple) {
            listener.clearSelectedFiles()
        }
        listener.selectFile(file, file !in selectedFiles)
    }

    fun selectAllFiles() {
        val files = fileItemSetOf()
        for (index in 0 until itemCount) {
            val file = getItem(index)
            if (isFileSelectable(file)) {
                files.add(file)
            }
        }
        listener.selectFiles(files, true)
    }

    private fun isFileSelectable(file: FileItem): Boolean {
        val pickOptions = pickOptions ?: return true
        return if (pickOptions.pickDirectory) {
            file.attributes.isDirectory
        } else {
            !file.attributes.isDirectory && pickOptions.mimeTypes.any { it.match(file.mimeType) }
        }
    }

    override fun clear() {
        super.clear()

        rebuildFilePositionMap()
    }

    @Deprecated("", ReplaceWith("replaceListAndSearching(list, searching)"))
    override fun replace(list: List<FileItem>, clear: Boolean) {
        throw UnsupportedOperationException()
    }

    fun replaceListAndIsSearching(list: List<FileItem>, isSearching: Boolean) {
        val clear = this.isSearching != isSearching
        this.isSearching = isSearching
        super.replace(if (!isSearching) list.sortedWith(comparator) else list, clear)
        rebuildFilePositionMap()
    }

    private fun rebuildFilePositionMap() {
        filePositionMap.clear()
        for (index in 0 until itemCount) {
            val file = getItem(index)
            filePositionMap[file.path] = index
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            FileItemBinding.inflate(parent.context.layoutInflater, parent, false)
        ).apply {
            binding.itemLayout.background = binding.itemLayout.context
                .getDrawableCompat(R.drawable.checkable_item_background)
            popupMenu = PopupMenu(binding.menuButton.context, binding.menuButton)
                .apply { inflate(R.menu.file_item) }
            binding.menuButton.setOnClickListener { popupMenu.show() }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        val file = getItem(position)
        val binding = holder.binding
        val isDirectory = file.attributes.isDirectory
        val enabled = isFileSelectable(file) || isDirectory
        binding.itemLayout.isEnabled = enabled
        binding.iconLayout.isEnabled = enabled
        binding.menuButton.isEnabled = enabled
        val menu = holder.popupMenu.menu
        val path = file.path
        val hasPickOptions = pickOptions != null
        val isReadOnly = path.fileSystem.isReadOnly
        menu.findItem(R.id.action_cut).isVisible = !hasPickOptions && !isReadOnly
        menu.findItem(R.id.action_copy).isVisible = !hasPickOptions
        val checked = file in selectedFiles
        binding.itemLayout.isChecked = checked
        if (payloads.isNotEmpty()) {
            return
        }
        bindViewHolderAnimation(holder)
        binding.itemLayout.setOnClickListener {
            if (selectedFiles.isEmpty()) {
                listener.openFile(file)
            } else {
                selectFile(file)
            }
        }
        binding.itemLayout.setOnLongClickListener {
            if (selectedFiles.isEmpty()) {
                selectFile(file)
            } else {
                listener.openFile(file)
            }
            true
        }
        binding.iconLayout.setOnClickListener { selectFile(file) }
        val attributes = file.attributes
        val icon = binding.iconImage.context.getDrawableCompat(file.mimeType.iconRes)
        if (file.supportsThumbnail) {
            binding.iconImage.loadAny(path to attributes) {
                placeholder(icon)
                ignoreError()
            }
        } else {
            binding.iconImage.clear()
            binding.iconImage.setImageDrawable(icon)
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
        binding.badgeImage.isVisible = hasBadge
        if (hasBadge) {
            binding.badgeImage.setImageResource(badgeIconRes!!)
        }
        binding.nameText.text = file.name
        binding.descriptionText.text = if (isDirectory) {
            null
        } else {
            val context = binding.descriptionText.context
            val lastModificationTime = attributes.lastModifiedTime().toInstant()
                .formatShort(context)
            val size = attributes.fileSize.formatHumanReadable(context)
            val descriptionSeparator = context.getString(R.string.file_item_description_separator)
            listOf(lastModificationTime, size).joinToString(descriptionSeparator)
        }
        val isArchivePath = path.isArchivePath
        menu.findItem(R.id.action_copy)
            .setTitle(if (isArchivePath) R.string.file_item_action_extract else R.string.copy)
        menu.findItem(R.id.action_delete).isVisible = !isReadOnly
        menu.findItem(R.id.action_rename).isVisible = !isReadOnly
        menu.findItem(R.id.action_extract).isVisible = file.isArchiveFile
        menu.findItem(R.id.action_add_bookmark).isVisible = isDirectory
        holder.popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_open_with -> {
                    listener.openFileWith(file)
                    true
                }
                R.id.action_cut -> {
                    listener.cutFile(file)
                    true
                }
                R.id.action_copy -> {
                    listener.copyFile(file)
                    true
                }
                R.id.action_delete -> {
                    listener.confirmDeleteFile(file)
                    true
                }
                R.id.action_rename -> {
                    listener.showRenameFileDialog(file)
                    true
                }
                R.id.action_extract -> {
                    listener.extractFile(file)
                    true
                }
                R.id.action_archive -> {
                    listener.showCreateArchiveDialog(file)
                    true
                }
                R.id.action_share -> {
                    listener.shareFile(file)
                    true
                }
                R.id.action_copy_path -> {
                    listener.copyPath(file)
                    true
                }
                R.id.action_add_bookmark -> {
                    listener.addBookmark(file)
                    true
                }
                R.id.action_create_shortcut -> {
                    listener.createShortcut(file)
                    true
                }
                R.id.action_properties -> {
                    listener.showPropertiesDialog(file)
                    true
                }
                else -> false
            }
        }
    }

    override fun getPopupText(position: Int): String {
        val file = getItem(position)
        return file.name.take(1).toUpperCase(Locale.getDefault())
    }

    override val isAnimationEnabled: Boolean
        get() = Settings.FILE_LIST_ANIMATION.valueCompat

    companion object {
        private val PAYLOAD_STATE_CHANGED = Any()

        private val CALLBACK = object : DiffUtil.ItemCallback<FileItem>() {
            override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem == newItem
        }
    }

    class ViewHolder(val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var popupMenu: PopupMenu
    }

    interface Listener {
        fun clearSelectedFiles()
        fun selectFile(file: FileItem, selected: Boolean)
        fun selectFiles(files: FileItemSet, selected: Boolean)
        fun openFile(file: FileItem)
        fun openFileWith(file: FileItem)
        fun cutFile(file: FileItem)
        fun copyFile(file: FileItem)
        fun confirmDeleteFile(file: FileItem)
        fun showRenameFileDialog(file: FileItem)
        fun extractFile(file: FileItem)
        fun showCreateArchiveDialog(file: FileItem)
        fun shareFile(file: FileItem)
        fun copyPath(file: FileItem)
        fun addBookmark(file: FileItem)
        fun createShortcut(file: FileItem)
        fun showPropertiesDialog(file: FileItem)
    }
}
