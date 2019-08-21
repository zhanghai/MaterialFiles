/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class FtpServerWakeLock {

    private static final String LOCK_TAG = FtpServerWakeLock.class.getSimpleName();

    @NonNull
    private final PowerManager.WakeLock mWakeLock;
    @NonNull
    private final WifiManager.WifiLock mWifiLock;

    public FtpServerWakeLock(@NonNull Context context) {
        PowerManager powerManager = ContextCompat.getSystemService(context, PowerManager.class);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
        mWakeLock.setReferenceCounted(false);
        // WifiManagerPotentialLeak
        WifiManager wifiManager = ContextCompat.getSystemService(context.getApplicationContext(),
                WifiManager.class);
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, LOCK_TAG);
        mWifiLock.setReferenceCounted(false);
    }

    public void acquire() {
        mWakeLock.acquire();
        mWifiLock.acquire();
    }

    public void release() {
        mWifiLock.release();
        mWakeLock.release();
    }
}
