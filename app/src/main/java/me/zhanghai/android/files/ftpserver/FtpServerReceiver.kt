/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.util.copyText

class FtpServerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            ACTION_STOP -> FtpServerService.stop(context)
            ACTION_COPY_URL -> {
                val url = FtpServerUrl.getUrl()
                if (url != null) {
                    clipboardManager.copyText(url, context)
                }
            }
            else -> throw IllegalArgumentException(action)
        }
    }

    companion object {
        const val ACTION_STOP = "stop"
        const val ACTION_COPY_URL = "copy_url"

        fun createIntent(): Intent =
            Intent(application, FtpServerReceiver::class.java)
                .setAction(ACTION_STOP)
    }
}
