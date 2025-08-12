/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import androidx.core.app.ServiceCompat
import me.zhanghai.android.files.app.notificationManager
import me.zhanghai.android.files.compat.stopForegroundCompat

class ForegroundNotificationManager(private val service: Service) {
    private val notifications = mutableMapOf<Int, Notification>()

    private var foregroundId = 0

    @SuppressLint("MissingPermission")
    fun notify(id: Int, notification: Notification) {
        synchronized(notifications) {
            if (notifications.isEmpty()) {
                service.startForeground(id, notification)
                notifications[id] = notification
                foregroundId = id
            } else {
                if (id == foregroundId) {
                    service.startForeground(id, notification)
                } else {
                    notificationManager.notify(id, notification)
                }
                notifications[id] = notification
            }
        }
    }

    fun cancel(id: Int) {
        synchronized(notifications) {
            if (id !in notifications) {
                return
            }
            if (id == foregroundId) {
                if (notifications.size == 1) {
                    service.stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
                    notifications -= id
                    foregroundId = 0
                } else {
                    notifications.entries.find { it.key != id }!!.let {
                        service.startForeground(it.key, it.value)
                        foregroundId = it.key
                    }
                    notificationManager.cancel(id)
                    notifications -= id
                }
            } else {
                notificationManager.cancel(id)
                notifications -= id
            }
        }
    }
}
