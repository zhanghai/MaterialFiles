/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.util.Log;

import androidx.annotation.NonNull;
import me.zhanghai.android.materialfilemanager.BuildConfig;

public class LogUtils {

    private static final String TAG = BuildConfig.APPLICATION_ID.substring(
            BuildConfig.APPLICATION_ID.lastIndexOf('.') + 1);

    private LogUtils() {}

    public static void d(@NonNull String message) {
        Log.d(TAG, buildMessage(message));
    }

    public static void e(@NonNull String message) {
        Log.e(TAG, buildMessage(message));
    }

    public static void i(@NonNull String message) {
        Log.i(TAG, buildMessage(message));
    }

    public static void v(@NonNull String message) {
        Log.v(TAG, buildMessage(message));
    }

    public static void w(@NonNull String message) {
        Log.w(TAG, buildMessage(message));
    }

    public static void wtf(@NonNull String message) {
        Log.wtf(TAG, buildMessage(message));
    }

    public static void println(@NonNull String message) {
        Log.println(Log.INFO, TAG, message);
    }

    @NonNull
    private static String buildMessage(@NonNull String rawMessage) {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String fullClassName = caller.getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        return className + "." + caller.getMethodName() + "(): " + rawMessage;
    }
}
