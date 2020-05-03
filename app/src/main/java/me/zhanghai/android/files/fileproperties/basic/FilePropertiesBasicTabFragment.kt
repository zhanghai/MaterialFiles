/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.basic

import android.os.Bundle
import androidx.lifecycle.observe
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatLong
import me.zhanghai.android.files.filelist.getMimeTypeName
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.fileproperties.FilePropertiesViewModel
import me.zhanghai.android.files.provider.archive.ArchiveFileAttributes
import me.zhanghai.android.files.provider.archive.archiveFile
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.viewModels

class FilePropertiesBasicTabFragment : FilePropertiesTabFragment() {
    private val viewModel by viewModels<FilePropertiesViewModel>({ requireParentFragment() })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        bindView(stateful) { file ->
            addItemView(R.string.file_properties_basic_name, file.name)
            val path = file.path
            if (path.isLinuxPath || path.isDocumentPath) {
                val parentPath = path.parent
                if (parentPath != null) {
                    addItemView(
                        R.string.file_properties_basic_parent_directory, parentPath.toString()
                    )
                }
            } else if (path.isArchivePath) {
                val archiveFile = path.archiveFile
                addItemView(R.string.file_properties_basic_archive_file, archiveFile.toFile().path)
                val attributes = file.attributes as ArchiveFileAttributes
                addItemView(R.string.file_properties_basic_archive_entry, attributes.entryName)
            }
            addItemView(R.string.file_properties_basic_type, getTypeText(file))
            val symbolicLinkTarget = file.symbolicLinkTarget
            if (symbolicLinkTarget != null) {
                addItemView(R.string.file_properties_basic_symbolic_link_target, symbolicLinkTarget)
            }
            addItemView(R.string.file_properties_basic_size, getSizeText(file))
            val lastModificationTime = file.attributes.lastModifiedTime().toInstant().formatLong()
            addItemView(R.string.file_properties_basic_last_modification_time, lastModificationTime)
        }
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
        val context = requireContext()
        val sizeInBytes = size.formatInBytes(context)
        return if (size.isHumanReadableInBytes) {
            sizeInBytes
        } else {
            val humanReadableSize = size.formatHumanReadable(context)
            getString(
                R.string.file_properties_basic_size_with_human_readable_format, humanReadableSize,
                sizeInBytes
            )
        }
    }
}
