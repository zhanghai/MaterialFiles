/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksum

import androidx.annotation.StringRes
import me.zhanghai.android.files.R
import java.security.MessageDigest

class ChecksumInfo(val checksums: Map<Algorithm, String>) {
    enum class Algorithm(@StringRes val nameRes: Int) {
        CRC32(R.string.file_properties_checksum_crc32),
        MD5(R.string.file_properties_checksum_md5),
        SHA1(R.string.file_properties_checksum_sha_1),
        SHA256(R.string.file_properties_checksum_sha_256),
        SHA512(R.string.file_properties_checksum_sha_512);

        fun createMessageDigest(): MessageDigest =
            when (this) {
                CRC32 -> Crc32MessageDigest()
                MD5 -> MessageDigest.getInstance("MD5")
                SHA1 -> MessageDigest.getInstance("SHA-1")
                SHA256 -> MessageDigest.getInstance("SHA-256")
                SHA512 -> MessageDigest.getInstance("SHA-512")
            }
    }
}
