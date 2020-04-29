/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

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
import me.zhanghai.android.files.databinding.FilePropertiesPermissionsTabFragmentBinding
import me.zhanghai.android.files.databinding.FilePropertiesPermissionsTabFragmentMd2Binding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.fileproperties.FilePropertiesViewModel
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.isNullOrEmpty
import me.zhanghai.android.files.provider.common.toInt
import me.zhanghai.android.files.provider.common.toModeString
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

class FilePropertiesPermissionsTabFragment : AppCompatDialogFragment() {
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
        val attributes = file.attributes
        if (attributes is PosixFileAttributes) {
            val owner = attributes.owner()
            binding.ownerText.text = if (owner != null) {
                if (owner.name != null) {
                    getString(
                        R.string.file_properties_permissions_principal_format, owner.name, owner.id
                    )
                } else {
                    owner.id.toString()
                }
            } else {
                getString(R.string.unknown)
            }
            binding.ownerText.setOnClickListener { SetOwnerDialogFragment.show(file, this) }
            val group = attributes.group()
            binding.groupText.text = if (group != null) {
                if (group.name != null) {
                    getString(
                        R.string.file_properties_permissions_principal_format, group.name, group.id
                    )
                } else {
                    group.id.toString()
                }
            } else {
                getString(R.string.unknown)
            }
            binding.groupText.setOnClickListener { SetGroupDialogFragment.show(file, this) }
            val mode = attributes.mode()
            binding.modeText.text = if (mode != null) {
                getString(
                    R.string.file_properties_permissions_mode_format, mode.toModeString(),
                    mode.toInt()
                )
            } else {
                getString(R.string.unknown)
            }
            binding.modeText.setOnClickListener {
                if (!attributes.isSymbolicLink()) {
                    SetModeDialogFragment.show(file, this)
                }
            }
            val seLinuxContext = attributes.seLinuxContext()
            binding.seLinuxContextLayout.isVisible= seLinuxContext != null
            binding.seLinuxContextText.text = if (!seLinuxContext.isNullOrEmpty()) {
                seLinuxContext.toString()
            } else {
                getString(R.string.empty_placeholder)
            }
            binding.seLinuxContextText.setOnClickListener {
                SetSeLinuxContextDialogFragment.show(file, this)
            }
        }
        // TODO: Other attributes?
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean {
            val attributes = file.attributes
            if (attributes !is PosixFileAttributes) {
                return false
            }
            return attributes.owner() != null || attributes.group() != null
                || attributes.mode() != null
        }
    }

    private class Binding private constructor(
        val root: View,
        val progress: ProgressBar,
        val errorText: TextView,
        val swipeRefreshLayout: SwipeRefreshLayout,
        val scrollView: NestedScrollView,
        val ownerText: TextView,
        val groupText: TextView,
        val modeText: TextView,
        val seLinuxContextLayout: ViewGroup,
        val seLinuxContextText: TextView
    ) {
        companion object {
            fun inflate(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean
            ): Binding =
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    val binding = FilePropertiesPermissionsTabFragmentMd2Binding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.ownerText,
                        binding.groupText, binding.modeText, binding.seLinuxContextLayout,
                        binding.seLinuxContextText
                    )
                } else {
                    val binding = FilePropertiesPermissionsTabFragmentBinding.inflate(
                        inflater, root, attachToRoot
                    )
                    Binding(
                        binding.root, binding.progress, binding.errorText,
                        binding.swipeRefreshLayout, binding.scrollView, binding.ownerText,
                        binding.groupText, binding.modeText, binding.seLinuxContextLayout,
                        binding.seLinuxContextText
                    )
                }
        }
    }
}
