/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksum

import androidx.core.widget.doAfterTextChanged
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.FilePropertiesChecksumCompareItemBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.viewModels

class FilePropertiesChecksumTabFragment : FilePropertiesTabFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesChecksumTabViewModel(args.path) } }

    override fun onResume() {
        super.onResume()

        viewModel.checksumInfoLiveData.observe(viewLifecycleOwner) { onChecksumInfoChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onChecksumInfoChanged(stateful: Stateful<ChecksumInfo>) {
        bindView(stateful) { checksumInfo ->
            checksumInfo.checksums.forEach { addItemView(it.key.nameRes, it.value) }
            addCompareEdit(checksumInfo)
        }
    }

    private fun ViewBuilder.addCompareEdit(checksumInfo: ChecksumInfo) {
        val binding = getScrapItemBinding(FilePropertiesChecksumCompareItemBinding::class.java)
            ?.also { addView(it) }
            ?: FilePropertiesChecksumCompareItemBinding.inflate(
                linearLayout.context.layoutInflater, linearLayout, true
            )
                .also { it.root.tag = it }
        binding.compareEdit.doAfterTextChanged { editable ->
            val text = editable!!.toString().trim()
            if (text.isEmpty()) {
                binding.compareLayout.helperText = null
                binding.compareLayout.error = null
                return@doAfterTextChanged
            }
            val matchingAlgorithm = checksumInfo.checksums.firstNotNullOfOrNull {
                if (it.value.equals(text, true)) it.key else null
            }
            if (matchingAlgorithm != null) {
                binding.compareLayout.helperText =
                    getString(
                        R.string.file_properties_checksum_compare_match_format,
                        getString(matchingAlgorithm.nameRes)
                    )
                return@doAfterTextChanged
            }
            val prefixMatchingAlgorithm = checksumInfo.checksums.firstNotNullOfOrNull {
                if (it.value.startsWith(text, true)) it.key else null
            }
            if (prefixMatchingAlgorithm != null) {
                binding.compareLayout.helperText =
                    getString(
                        R.string.file_properties_checksum_compare_prefix_match_format,
                        getString(prefixMatchingAlgorithm.nameRes)
                    )
                return@doAfterTextChanged
            }
            binding.compareLayout.error =
                getString(R.string.file_properties_checksum_compare_no_match)
        }
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean = file.attributes.isRegularFile
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs
}
