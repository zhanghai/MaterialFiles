/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.FilePropertiesBasicTabFragmentBinding
import me.zhanghai.android.files.databinding.FilePropertiesBasicTabFragmentMd2Binding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatLong
import me.zhanghai.android.files.filelist.getMimeTypeName
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.fileproperties.FilePropertiesViewModel
import me.zhanghai.android.files.provider.archive.ArchiveFileAttributes
import me.zhanghai.android.files.provider.archive.archiveFile
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.viewModels

class FilePropertiesBasicTabFragment : AppCompatDialogFragment() {
    private val viewModel by viewModels<FilePropertiesViewModel>({ requireParentFragment() })

    private lateinit var binding: Binding

    private var lastIsSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        Binding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener { refresh() }

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    private fun refresh() {
        viewModel.reloadFile()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        when (stateful) {
            is Loading -> {
                binding.progress.fadeToVisibilityUnsafe(!lastIsSuccess)
                if (lastIsSuccess) {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                binding.errorText.fadeOutUnsafe()
                if (!lastIsSuccess) {
                    binding.scrollView.isInvisible = true
                }
            }
            is Failure -> {
                binding.progress.fadeOutUnsafe()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.errorText.fadeInUnsafe()
                binding.errorText.text = stateful.throwable.toString()
                binding.scrollView.fadeOutUnsafe()
                lastIsSuccess = false
            }
            is Success -> {
                binding.progress.fadeOutUnsafe()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.errorText.fadeOutUnsafe()
                binding.scrollView.fadeInUnsafe()
                updateView(stateful.value)
                lastIsSuccess = true
            }
        }
    }

    private fun updateView(file: FileItem) {
        binding.nameText.text = file.name
        val path = file.path
        if (path.isLinuxPath || path.isDocumentPath) {
            val parentPath = path.parent
            if (parentPath != null) {
                binding.parentDirectoryLayout.isVisible = true
                binding.parentDirectoryText.text = parentPath.toString()
            }
        } else if (path.isArchivePath) {
            binding.archiveFileAndEntryLayout.isVisible = true
            val archiveFile = path.archiveFile
            binding.archiveFileText.text = archiveFile.toFile().path
            val attributes = file.attributes as ArchiveFileAttributes
            binding.archiveEntryText.text = attributes.entryName
        }
        binding.typeText.text = getTypeText(file)
        val isSymbolicLink = file.attributesNoFollowLinks.isSymbolicLink
        binding.symbolicLinkTargetLayout.isVisible = isSymbolicLink
        if (isSymbolicLink) {
            binding.symbolicLinkTargetText.text = file.symbolicLinkTarget
        }
        binding.sizeText.text = getSizeText(file)
        val lastModificationTime = file.attributes.lastModifiedTime().toInstant().formatLong()
        binding.lastModificationTimeText.text = lastModificationTime
    }

    private fun getTypeText(file: FileItem): String {
        val typeFormatRes = if (file.attributesNoFollowLinks.isSymbolicLink
            && !file.isSymbolicLinkBroken) {
            R.string.file_properties_basic_type_symbolic_link_format
        } else {
            R.string.file_properties_basic_type_format
        }
        return getString(typeFormatRes, file.getMimeTypeName(requireContext()), file.mimeType.value)
    }

    private fun getSizeText(file: FileItem): String {
        val size = file.attributes.fileSize
        val sizeInBytes = size.formatInBytes(binding.sizeText.context)
        return if (size.isHumanReadableInBytes) {
            sizeInBytes
        } else {
            val humanReadableSize = size.formatHumanReadable(binding.sizeText.context)
            getString(
                R.string.file_properties_basic_size_with_human_readable_format, humanReadableSize,
                sizeInBytes
            )
        }
    }

    private class Binding private constructor(
        val root: View,
        val progress: ProgressBar,
        val errorText: TextView,
        val swipeRefreshLayout: SwipeRefreshLayout,
        val scrollView: NestedScrollView,
        val nameText: TextView,
        val parentDirectoryLayout: ViewGroup,
        val parentDirectoryText: TextView,
        val archiveFileAndEntryLayout: ViewGroup,
        val archiveFileText: TextView,
        val archiveEntryText: TextView,
        val typeText: TextView,
        val symbolicLinkTargetLayout: ViewGroup,
        val symbolicLinkTargetText: TextView,
        val sizeText: TextView,
        val lastModificationTimeText: TextView
    ) {
        companion object {
            fun inflate(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean
            ): Binding =
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    val binding = FilePropertiesBasicTabFragmentMd2Binding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.nameText,
                        binding.parentDirectoryLayout, binding.parentDirectoryText,
                        binding.archiveFileAndEntryLayout, binding.archiveFileText,
                        binding.archiveEntryText, binding.typeText,
                        binding.symbolicLinkTargetLayout, binding.symbolicLinkTargetText,
                        binding.sizeText, binding.lastModificationTimeText
                    )
                } else {
                    val binding = FilePropertiesBasicTabFragmentBinding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.nameText,
                        binding.parentDirectoryLayout, binding.parentDirectoryText,
                        binding.archiveFileAndEntryLayout, binding.archiveFileText,
                        binding.archiveEntryText, binding.typeText,
                        binding.symbolicLinkTargetLayout, binding.symbolicLinkTargetText,
                        binding.sizeText, binding.lastModificationTimeText
                    )
                }
        }
    }
}
