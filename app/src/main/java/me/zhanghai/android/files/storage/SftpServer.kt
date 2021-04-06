/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import java8.nio.file.Path
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.provider.sftp.client.Authentication
import me.zhanghai.android.files.provider.sftp.client.Authority
import me.zhanghai.android.files.provider.sftp.createSftpRootPath
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.putArgs
import kotlin.random.Random

@Parcelize
class SftpServer(
    override val id: Long,
    override val customName: String?,
    val authority: Authority,
    val authentication: Authentication
) : Storage() {
    constructor(
        id: Long?,
        customName: String?,
        authority: Authority,
        authentication: Authentication
    ) : this(id ?: Random.nextLong(), customName, authority, authentication)

    @DrawableRes
    @IgnoredOnParcel
    override val iconRes: Int = R.drawable.computer_icon_white_24dp

    override fun getDefaultName(context: Context): String = authority.toString()

    override val description: String
        get() = authority.toString()

    override val path: Path
        get() = authority.createSftpRootPath()

    override fun createEditIntent(): Intent =
        EditSftpServerActivity::class.createIntent().putArgs(EditSftpServerFragment.Args(this))
}
