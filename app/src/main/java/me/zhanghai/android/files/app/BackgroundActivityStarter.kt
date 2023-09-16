/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.NotificationChannelTemplate
import me.zhanghai.android.files.util.NotificationTemplate
import me.zhanghai.android.files.util.startActivitySafe

val backgroundActivityStartNotificationTemplate =
    NotificationTemplate(
        NotificationChannelTemplate(
            "background_activity_start",
            R.string.notification_channel_background_activity_start_name,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            descriptionRes = R.string.notification_channel_background_activity_start_description,
            showBadge = false
        ),
        colorRes = R.color.color_primary,
        smallIcon = R.drawable.notification_icon,
        ongoing = true,
        autoCancel = true,
        category = NotificationCompat.CATEGORY_ERROR,
        priority = NotificationCompat.PRIORITY_HIGH
    )

object BackgroundActivityStarter {
    fun startActivity(intent: Intent, title: CharSequence, text: CharSequence?, context: Context) {
        // TODO: Only use new task when in background?
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isInForeground) {
            context.startActivitySafe(intent)
        } else {
            notifyStartActivity(intent, title, text, context)
        }
    }

    private val isInForeground: Boolean
        get() = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
            Lifecycle.State.STARTED
        )

    private fun notifyStartActivity(
        intent: Intent,
        title: CharSequence,
        text: CharSequence?,
        context: Context
    ) {
        var pendingIntentFlags = PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }
        val pendingIntent = PendingIntent.getActivity(
            context, intent.hashCode(), intent, pendingIntentFlags
        )
        val notification = backgroundActivityStartNotificationTemplate.createBuilder(context)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(intent.hashCode(), notification)
    }
}
