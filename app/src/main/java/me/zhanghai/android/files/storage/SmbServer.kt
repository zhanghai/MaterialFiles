/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.provider.smb.client.Authentication
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.provider.smb.createSmbRootPath
import kotlin.random.Random

@Parcelize
class SmbServer(
    override val id: Long,
    override val name: String,
    val authority: Authority,
    val authentication: Authentication
) : Storage {
    constructor(
        id: Long?,
        name: String,
        authority: Authority,
        authentication: Authentication
    ) : this(id ?: Random.nextLong(), name, authority, authentication)

    override val description: String
        get() = authority.toString()
    override val path: Path
        get() = authority.createSmbRootPath()
}
