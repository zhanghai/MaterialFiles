package me.zhanghai.android.files.util

import android.os.storage.StorageVolume
import me.zhanghai.android.files.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
