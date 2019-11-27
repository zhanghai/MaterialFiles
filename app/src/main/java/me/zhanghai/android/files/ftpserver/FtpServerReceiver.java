/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import androidx.annotation.NonNull;

public class FtpServerReceiver extends BroadcastReceiver {

    private static final String KEY_PREFIX = FtpServerReceiver.class.getName() + '.';

    public static final String ACTION_STOP = KEY_PREFIX + "STOP";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (Objects.equals(action, ACTION_STOP)) {
            FtpServerService.stop(context);
        } else {
            throw new IllegalArgumentException(action);
        }
    }
}
