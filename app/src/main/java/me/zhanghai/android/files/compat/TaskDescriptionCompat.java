/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.app.ActivityManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedMethod;

public class TaskDescriptionCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedMethod<ActivityManager.TaskDescription> sSetPrimaryColorMethod =
            new ReflectedMethod<>(ActivityManager.TaskDescription.class, "setPrimaryColor",
                    int.class);

    private TaskDescriptionCompat() {}

    public static void setPrimaryColor(@NonNull ActivityManager.TaskDescription taskDescription,
                                       @ColorInt int primaryColor) {
        sSetPrimaryColorMethod.invoke(taskDescription, primaryColor);
    }
}
