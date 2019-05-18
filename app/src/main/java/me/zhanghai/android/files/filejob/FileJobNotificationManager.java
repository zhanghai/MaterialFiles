/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Service;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import me.zhanghai.android.files.notification.Notifications;

public class FileJobNotificationManager {

    public static final String CHANNEL_ID = Notifications.Channels.FILE_JOB.ID;

    @NonNull
    private final Service mService;

    @NonNull
    private final NotificationManagerCompat mNotificationManager;

    @NonNull
    private final Object mLock = new Object();

    private boolean mChannelCreated;

    @NonNull
    private final Map<Integer, Notification> mNotifications = new HashMap<>();
    private int mForegroundId;

    public FileJobNotificationManager(@NonNull Service service) {
        mService = service;

        mNotificationManager = NotificationManagerCompat.from(mService);
    }

    private void ensureChannelLocked() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        if (mChannelCreated) {
            return;
        }
        @SuppressLint("WrongConstant")
        NotificationChannel channel = new NotificationChannel(Notifications.Channels.FILE_JOB.ID,
                mService.getString(Notifications.Channels.FILE_JOB.NAME_RES),
                Notifications.Channels.FILE_JOB.IMPORTANCE);
        channel.setDescription(mService.getString(Notifications.Channels.FILE_JOB.DESCRIPTION_RES));
        channel.setShowBadge(false);
        mNotificationManager.createNotificationChannel(channel);
        mChannelCreated = true;
    }

    public void notify(int id, @NonNull Notification notification) {
        synchronized (mLock) {
            ensureChannelLocked();
            if (mNotifications.isEmpty()) {
                mService.startForeground(id, notification);
                mNotifications.put(id, notification);
                mForegroundId = id;
            } else {
                if (id == mForegroundId) {
                    mService.startForeground(id, notification);
                } else {
                    mNotificationManager.notify(id, notification);
                }
                mNotifications.put(id, notification);
            }
        }
    }

    public void cancel(int id) {
        synchronized (mLock) {
            if (!mNotifications.containsKey(id)) {
                return;
            }
            if (id == mForegroundId) {
                if (mNotifications.size() == 1) {
                    mService.stopForeground(true);
                    mNotifications.remove(id);
                    mForegroundId = 0;
                } else {
                    for (Map.Entry<Integer, Notification> entry : mNotifications.entrySet()) {
                        int entryId = entry.getKey();
                        if (entryId == id) {
                            continue;
                        }
                        mService.startForeground(entryId, entry.getValue());
                        mForegroundId = entryId;
                        break;
                    }
                    mNotificationManager.cancel(id);
                    mNotifications.remove(id);
                }
            } else {
                mNotificationManager.cancel(id);
                mNotifications.remove(id);
            }
        }
    }
}
