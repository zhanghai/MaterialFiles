/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.app.StatusBarManager
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.getSystemServiceCompat
import me.zhanghai.android.files.compat.mainExecutorCompat
import me.zhanghai.android.files.util.requestAddTileService
import me.zhanghai.android.files.util.showToast

class FtpServerAddTilePreference : Preference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isPersistent = false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick() {
        val statusBarManager = context.getSystemServiceCompat(StatusBarManager::class.java)
        statusBarManager.requestAddTileService(
            FtpServerTileService::class.java, context.mainExecutorCompat
        ) { result ->
            val resultRes = when (result) {
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED ->
                    return@requestAddTileService
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                    R.string.ftp_server_add_tile_result_already_added
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ->
                    R.string.ftp_server_add_tile_result_added
                else -> R.string.ftp_server_add_tile_result_error
            }
            context.showToast(resultRes)
        }
    }
}
