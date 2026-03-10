/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import me.zhanghai.android.files.util.createIntent

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
        val tile = qsTile ?: return
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

        val intent = FtpServerBiometricActivity::class.createIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            if (isLocked) {
                unlockAndRun {
                    startActivityAndCollapse(intent)
                }
            } else {
                startActivityAndCollapse(intent)
            }
        }
    }
}
