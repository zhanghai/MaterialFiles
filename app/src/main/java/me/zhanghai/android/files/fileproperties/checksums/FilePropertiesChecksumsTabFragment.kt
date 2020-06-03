/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * Copyright 2020 Panagiotis Panagiotopoulos <panagiotopoulos.git@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksums

import android.os.Bundle
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.file_properties_tab_item.view.*
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.fileproperties.FilePropertiesFileViewModel
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.viewModels
import java.io.FileInputStream
import java.security.MessageDigest

class FilePropertiesChecksumsTabFragment : FilePropertiesTabFragment() {
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
            addItemView(
                    R.string.file_properties_checksums_md5,
                    ""
            ) {
                if(it.textText.text.isNullOrEmpty())
                    it.textText.setText(getFileHash(file.path.toString(), "MD5"))
            }
            addItemView(
                    R.string.file_properties_checksums_sha1,
                    ""
            ) {
                if(it.textText.text.isNullOrEmpty())
                    it.textText.setText(getFileHash(file.path.toString(), "SHA-1"))
            }
            addItemView(
                    R.string.file_properties_checksums_sha256,
                    ""
            ) {
                if(it.textText.text.isNullOrEmpty())
                    it.textText.setText(getFileHash(file.path.toString(), "SHA-256"))
            }
        }
    }

    private fun getFileHash(filePath: String, algorithm: String): String {
        val md = MessageDigest.getInstance(algorithm)
        val fis = FileInputStream(filePath)
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

    companion object {
        fun isAvailable(file: FileItem): Boolean = file.attributes.isRegularFile
    }
}
