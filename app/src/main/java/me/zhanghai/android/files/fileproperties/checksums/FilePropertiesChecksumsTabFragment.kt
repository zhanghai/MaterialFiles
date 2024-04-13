/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * Copyright (c) 2020, 2024 Panagiotis Panagiotopoulos <panagiotopoulos.git@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksums

import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import java8.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.fileproperties.FilePropertiesFileViewModel
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.copyText
import me.zhanghai.android.files.util.viewModels
import java.io.FileInputStream
import java.security.MessageDigest

class FilePropertiesChecksumsTabFragment : FilePropertiesTabFragment() {
    private val viewModel by viewModels<FilePropertiesFileViewModel>({ requireParentFragment() })

    private val jobs: MutableList<Job> = mutableListOf()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        bindView(stateful) { file ->
            addChecksumView(this, file, ChecksumType.MD5)
            addChecksumView(this, file, ChecksumType.SHA_1)
            addChecksumView(this, file, ChecksumType.SHA_256)
            addChecksumView(this, file, ChecksumType.SHA_512)
        }
    }

    private fun addChecksumView(
        builder: ViewBuilder,
        file: FileItem,
        type: ChecksumType
    ): TextView {
        return builder.addItemView(type.hintRes, "") { view ->
            if (view is TextView && view.text.isNullOrEmpty()) {
                view.text = getString(R.string.file_properties_checksums_calculating)
                val job = viewLifecycleOwner.lifecycleScope.launch {
                    val checksum = withContext(Dispatchers.IO) { type.getFileHash(file.path) }
                    view.text = checksum
                    view.setOnLongClickListener {
                        clipboardManager.copyText(checksum, binding.root.context)
                        true
                    }
                }
                jobs.add(job)
            }
        }
    }

    private enum class ChecksumType(val algorithm: String, @StringRes val hintRes: Int) {
        MD5("MD5", R.string.file_properties_checksums_md5),
        SHA_1("SHA-1", R.string.file_properties_checksums_sha1),
        SHA_256("SHA-256", R.string.file_properties_checksums_sha256),
        SHA_512("SHA-512", R.string.file_properties_checksums_sha512);

        fun getFileHash(filePath: Path): String {
            val md = MessageDigest.getInstance(algorithm)
            val fis = FileInputStream(filePath.toString())
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
            fis.close()
            return md.digest().toHex()
        }

        private fun ByteArray.toHex(): String {
            return joinToString("") { "%02x".format(it) }
        }
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean = file.attributes.isRegularFile
    }
}
