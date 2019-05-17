/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import androidx.annotation.NonNull;

public class FileJobReceiver extends BroadcastReceiver {

    private static final String KEY_PREFIX = FileJobReceiver.class.getName() + '.';

    public static final String ACTION_CANCEL = KEY_PREFIX + "CANCEL";

    public static final String EXTRA_JOB_ID = KEY_PREFIX + "JOB_ID";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        int jobId = intent.getIntExtra(EXTRA_JOB_ID, 0);
        if (Objects.equals(action, ACTION_CANCEL)) {
            FileJobService.cancelRunningJob(jobId);
        } else {
            throw new IllegalArgumentException(action);
        }
    }
}
