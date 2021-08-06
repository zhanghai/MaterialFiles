/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import android.os.Bundle
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.fileproperties.FilePropertiesFileViewModel
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.PosixPrincipal
import me.zhanghai.android.files.provider.common.toInt
import me.zhanghai.android.files.provider.common.toModeString
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.viewModels

class FilePropertiesPermissionsTabFragment : FilePropertiesTabFragment() {
    private val viewModel by viewModels<FilePropertiesFileViewModel>({ requireParentFragment() })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        bindView(stateful) { file ->
            val attributes = file.attributes as PosixFileAttributes
            val owner = attributes.owner()
            addItemView(
                R.string.file_properties_permissions_owner, getPrincipalText(owner), owner?.let {
                    { SetOwnerDialogFragment.show(file, this@FilePropertiesPermissionsTabFragment) }
                }
            )
            val group = attributes.group()
            addItemView(
                R.string.file_properties_permissions_group, getPrincipalText(group), group?.let {
                    { SetGroupDialogFragment.show(file, this@FilePropertiesPermissionsTabFragment) }
                }
            )
            val mode = attributes.mode()
            addItemView(
                R.string.file_properties_permissions_mode, if (mode != null) {
                    getString(
                        R.string.file_properties_permissions_mode_format, mode.toModeString(),
                        mode.toInt()
                    )
                } else {
                    getString(R.string.unknown)
                }, if (mode != null && !attributes.isSymbolicLink) {
                    { SetModeDialogFragment.show(file, this@FilePropertiesPermissionsTabFragment) }
                } else {
                    null
                }
            )
            val seLinuxContext = attributes.seLinuxContext()
            if (seLinuxContext != null) {
                addItemView(
                    R.string.file_properties_permissions_selinux_context,
                    if (seLinuxContext.isNotEmpty()) {
                        seLinuxContext.toString()
                    } else {
                        getString(R.string.empty_placeholder)
                    }
                ) {
                    SetSeLinuxContextDialogFragment.show(
                        file, this@FilePropertiesPermissionsTabFragment
                    )
                }
            }
        }
    }

    private fun getPrincipalText(principal: PosixPrincipal?) =
        if (principal != null) {
            if (principal.name != null) {
                getString(
                    R.string.file_properties_permissions_principal_format, principal.name,
                    principal.id
                )
            } else {
                principal.id.toString()
            }
        } else {
            getString(R.string.unknown)
        }

    companion object {
        fun isAvailable(file: FileItem): Boolean {
            val attributes = file.attributes
            return attributes is PosixFileAttributes && (attributes.owner() != null
                || attributes.group() != null || attributes.mode() != null
                || attributes.seLinuxContext() != null)
        }
    }
}
