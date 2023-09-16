/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.app.NotificationChannel
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import me.zhanghai.android.files.compat.getColorCompat

class NotificationTemplate(
    val channelTemplate: NotificationChannelTemplate,
    @ColorRes val colorRes: Int? = null,
    @DrawableRes val smallIcon: Int? = null,
    @StringRes val contentTitleRes: Int? = null,
    @StringRes val contentTextRes: Int? = null,
    val ongoing: Boolean? = null,
    val onlyAlertOnce: Boolean? = null,
    val autoCancel: Boolean? = null,
    val category: String? = null,
    val priority: Int? = null
) {
    fun createBuilder(context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelTemplate.id).apply {
            colorRes?.let { setColor(context.getColorCompat(it)) }
            smallIcon?.let { setSmallIcon(it) }
            contentTitleRes?.let { setContentTitle(context.getText(contentTitleRes)) }
            contentTextRes?.let { setContentText(context.getText(contentTextRes)) }
            ongoing?.let { setOngoing(it) }
            onlyAlertOnce?.let { setOnlyAlertOnce(it) }
            autoCancel?.let { setAutoCancel(it) }
            category?.let { setCategory(it) }
            this@NotificationTemplate.priority?.let { priority = it }
        }
}

class NotificationChannelTemplate(
    val id: String,
    @StringRes val nameRes: Int,
    val importance: Int,
    @StringRes val descriptionRes: Int? = null,
    val group: String? = null,
    val showBadge: Boolean? = null,
    val sound: Pair<Uri, AudioAttributes>? = null,
    val lightEnabled: Boolean? = null,
    val lightColor: Int? = null,
    val vibrationEnabled: Boolean? = null,
    val vibrationPattern: LongArray? = null,
    val bypassDnd: Boolean? = null,
    val lockscreenVisibility: Int? = null,
    val allowBubbles: Boolean? = null
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun create(context: Context): NotificationChannel =
        NotificationChannel(id, context.getString(nameRes), importance).apply {
            descriptionRes?.let { description = context.getString(it) }
            this@NotificationChannelTemplate.group?.let { group = it }
            showBadge?.let { setShowBadge(it) }
            this@NotificationChannelTemplate.sound?.let { setSound(it.first, it.second) }
            lightEnabled?.let { enableLights(it) }
            this@NotificationChannelTemplate.lightColor?.let { lightColor = it }
            vibrationEnabled?.let { enableVibration(vibrationEnabled) }
            this@NotificationChannelTemplate.vibrationPattern?.let { vibrationPattern = it }
            bypassDnd?.let { setBypassDnd(it) }
            this@NotificationChannelTemplate.lockscreenVisibility?.let { lockscreenVisibility = it }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                allowBubbles?.let { setAllowBubbles(it) }
            }
        }
}
