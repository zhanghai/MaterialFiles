/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.notification.Notifications;

class FtpServerServiceNotification {

    private static boolean sNotificationChannelCreated;

    private FtpServerServiceNotification() {}

    public static void startForeground(@NonNull Service service) {
        ensureNotificationChannel(service);
        Intent stopIntent = new Intent(service, FtpServerReceiver.class)
                .setAction(FtpServerReceiver.ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(service,
                FtpServerReceiver.class.hashCode(), stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(service,
                Notifications.Channels.FTP_SERVER.ID)
                .setColor(ContextCompat.getColor(service, R.color.color_primary))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(service.getString(R.string.ftp_server_notification_title))
                .setContentText(service.getString(R.string.ftp_server_notification_text))
                .setContentIntent(/* TODO: FtpServerActivity.class */ null)
                .setOngoing(true)
                .setCategory(Notifications.Channels.FTP_SERVER.CATEGORY)
                .setPriority(Notifications.Channels.FTP_SERVER.PRIORITY)
                .addAction(R.drawable.stop_icon_white_24dp, service.getString(R.string.stop),
                        stopPendingIntent)
                .build();
        service.startForeground(Notifications.Ids.FTP_SERVER, notification);
    }

    private static void ensureNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        if (sNotificationChannelCreated) {
            return;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        @SuppressLint("WrongConstant")
        NotificationChannel channel = new NotificationChannel(
                Notifications.Channels.FTP_SERVER.ID, context.getString(
                Notifications.Channels.FTP_SERVER.NAME_RES),
                Notifications.Channels.FTP_SERVER.IMPORTANCE);
        channel.setDescription(context.getString(
                Notifications.Channels.FTP_SERVER.DESCRIPTION_RES));
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
        sNotificationChannelCreated = true;
    }

    public static void stopForeground(@NonNull Service service) {
        service.stopForeground(true);
    }
}
