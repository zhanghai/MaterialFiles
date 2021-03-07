/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
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
import me.zhanghai.android.files.provider.smb.client.Authentication
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.provider.smb.createSmbRootPath
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.putArgs
import kotlin.random.Random

@Parcelize
class SmbServer(
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
        get() = authority.createSmbRootPath()

    override fun createEditIntent(): Intent =
        EditSmbServerActivity::class.createIntent().putArgs(EditSmbServerFragment.Args(this))
}
