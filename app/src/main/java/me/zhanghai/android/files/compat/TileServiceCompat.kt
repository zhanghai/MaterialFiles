/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import me.zhanghai.android.files.hiddenapi.RestrictedHiddenApi
import me.zhanghai.android.files.util.lazyReflectedField

@delegate:RequiresApi(Build.VERSION_CODES.N)
@get:RequiresApi(Build.VERSION_CODES.N)
@RestrictedHiddenApi
private val tokenField by lazyReflectedField(TileService::class.qualifiedName!!, "mToken")

val TileService.token: IBinder?
    @RequiresApi(Build.VERSION_CODES.N)
    get() = tokenField.get(this) as IBinder?
