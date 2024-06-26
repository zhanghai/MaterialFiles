/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.graphics.PixelFormat
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import me.zhanghai.android.files.compat.WindowManagerLayoutParamsCompat
import me.zhanghai.android.files.compat.token

@RequiresApi(Build.VERSION_CODES.N)
class FtpServerTileService : TileService() {
    private val observer = Observer<FtpServerService.State> { onFtpServerStateChanged(it) }

    override fun onStartListening() {
        super.onStartListening()

        FtpServerService.stateLiveData.observeForever(observer)
    }

    override fun onStopListening() {
        super.onStopListening()

        FtpServerService.stateLiveData.removeObserver(observer)
    }

    private fun onFtpServerStateChanged(state: FtpServerService.State) {
        val tile = qsTile
        when (state) {
            FtpServerService.State.STARTING,
            FtpServerService.State.RUNNING -> tile.state = Tile.STATE_ACTIVE
            FtpServerService.State.STOPPING -> tile.state = Tile.STATE_UNAVAILABLE
            FtpServerService.State.STOPPED -> tile.state = Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (isLocked) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runWithForegroundWindow { FtpServerService.toggle(this) }
        } else {
            FtpServerService.toggle(this)
        }
    }

    // Work around https://issuetracker.google.com/issues/299506164 on U which is fixed in V.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun runWithForegroundWindow(block: () -> Unit) {
        val windowManager = getSystemService(WindowManager::class.java)
        val view = View(this)
        val layoutParams =
            WindowManager.LayoutParams().apply {
                type = WindowManagerLayoutParamsCompat.TYPE_QS_DIALOG
                format = PixelFormat.TRANSLUCENT
                token = this@FtpServerTileService.token
            }
        windowManager.addView(view, layoutParams)
        // We need to wait for WindowState.onSurfaceShownChanged(), basically when the first draw
        // has finished and the surface is about to be shown to the user. However there's no good
        // callback for that, while waiting for the second pre-draw seems to work.
        view.doOnPreDraw {
            view.post {
                view.invalidate()
                view.doOnPreDraw {
                    try {
                        block()
                    } finally {
                        windowManager.removeView(view)
                    }
                }
            }
        }
    }
}
