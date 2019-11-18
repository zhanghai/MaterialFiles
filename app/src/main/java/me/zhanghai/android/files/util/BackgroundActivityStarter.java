/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.notification.Notifications;

public class BackgroundActivityStarter {

    private static transient boolean sNotificationChannelCreated;

    private BackgroundActivityStarter() {}

    public static void startActivity(@NonNull Intent intent, @NonNull CharSequence title,
                                     @Nullable CharSequence text, @NonNull Context context) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isInForeground()) {
            AppUtils.startActivity(intent, context);
        } else {
            notifyStartActivity(intent, title, text, context);
        }
    }

    private static boolean isInForeground() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(
                Lifecycle.State.STARTED);
    }

    private static void notifyStartActivity(@NonNull Intent intent, @NonNull CharSequence title,
                                            @Nullable CharSequence text, @NonNull Context context) {
        ensureNotificationChannel(context);
        int pendingIntentFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, intent.hashCode(),
                intent, pendingIntentFlags);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(context,
                Notifications.Channels.BACKGROUND_ACTIVITY_START.ID)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setCategory(Notifications.Channels.BACKGROUND_ACTIVITY_START.CATEGORY)
                .setPriority(Notifications.Channels.BACKGROUND_ACTIVITY_START.PRIORITY)
                .build();
        notificationManager.notify(intent.hashCode(), notification);
    }

    private static void ensureNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        // We can afford the possible race condition here.
        if (sNotificationChannelCreated) {
            return;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        @SuppressLint("WrongConstant")
        NotificationChannel channel = new NotificationChannel(
                Notifications.Channels.BACKGROUND_ACTIVITY_START.ID, context.getString(
                Notifications.Channels.BACKGROUND_ACTIVITY_START.NAME_RES),
                Notifications.Channels.BACKGROUND_ACTIVITY_START.IMPORTANCE);
        channel.setDescription(context.getString(
                Notifications.Channels.BACKGROUND_ACTIVITY_START.DESCRIPTION_RES));
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
        sNotificationChannelCreated = true;
    }
}
