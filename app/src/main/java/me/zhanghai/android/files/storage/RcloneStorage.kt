/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.provider.rclone.RcloneAuthority
import me.zhanghai.android.files.provider.rclone.createRcloneRootPath
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.putArgs
import kotlin.random.Random

@Parcelize
data class RcloneStorage(
    override val id: Long,
    override val customName: String?,
    val remoteName: String,
    val relativePath: String = ""
) : Storage() {
    constructor(
        id: Long?,
        customName: String?,
        remoteName: String,
        relativePath: String = ""
    ) : this(id ?: Random.nextLong(), customName, remoteName, relativePath)

    override val iconRes: Int
        @DrawableRes
        get() = R.drawable.cloud_icon_white_24dp

    override fun getDefaultName(context: Context): String =
        if (relativePath.isNotEmpty()) "$remoteName/$relativePath" else remoteName

    override val description: String
        get() = "Rclone: $remoteName"

    override val path: Path
        get() = if (relativePath.isNotEmpty()) {
            RcloneAuthority(remoteName).createRcloneRootPath().resolve(relativePath)
        } else {
            RcloneAuthority(remoteName).createRcloneRootPath()
        }

    override fun createEditIntent(): Intent =
        EditRcloneStorageDialogActivity::class.createIntent()
            .putArgs(EditRcloneStorageDialogFragment.Args(this))
}
