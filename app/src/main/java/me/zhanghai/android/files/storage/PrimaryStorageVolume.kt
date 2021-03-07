/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.Context
import android.os.storage.StorageVolume
import androidx.annotation.DrawableRes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.getDescriptionCompat
import me.zhanghai.android.files.compat.isPrimaryCompat
import me.zhanghai.android.files.compat.pathCompat
import me.zhanghai.android.files.util.valueCompat

@Parcelize
class PrimaryStorageVolume(
    override val customName: String?,
    override val isVisible: Boolean
) : LinuxStorage() {
    @IgnoredOnParcel
    override val id: Long = "PrimaryStorageVolume".hashCode().toLong()

    @DrawableRes
    @IgnoredOnParcel
    override val iconRes: Int = R.drawable.sd_card_icon_white_24dp

    override fun getDefaultName(context: Context): String =
        storageVolume.getDescriptionCompat(context)

    override val linuxPath: String
        get() = storageVolume.pathCompat

    private val storageVolume: StorageVolume
        get() = StorageVolumeListLiveData.valueCompat.find { it.isPrimaryCompat }!!
}
