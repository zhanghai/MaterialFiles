/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.Context
import androidx.annotation.DrawableRes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R

@Parcelize
class FileSystemRoot(
    override val customName: String?,
    override val isVisible: Boolean
) : LinuxStorage() {
    @IgnoredOnParcel
    override val id: Long = "FileSystemRoot".hashCode().toLong()

    @DrawableRes
    @IgnoredOnParcel
    override val iconRes: Int = R.drawable.device_icon_white_24dp

    override fun getDefaultName(context: Context): String =
        context.getString(R.string.storage_file_system_root_title)

    @IgnoredOnParcel
    override val linuxPath: String = LINUX_PATH

    companion object {
        const val LINUX_PATH = "/"
    }
}
