/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegateCompat;
import androidx.core.app.ActivityCompat;
import me.zhanghai.android.files.settings.SettingsLiveDatas;

public class NightModeHelper {

    private static final HashSet<AppCompatActivity> sActivities = new HashSet<>();

    private NightModeHelper() {}

    public static void initialize(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(@NonNull Activity activity,
                                                  @Nullable Bundle savedInstanceState) {
                        if (!sActivities.contains(activity)) {
                            throw new IllegalStateException("Activity must extend AppActivity:"
                                    + activity);
                        }
                    }
                    @Override
                    public void onActivityStarted(@NonNull Activity activity) {}
                    @Override
                    public void onActivityResumed(@NonNull Activity activity) {}
                    @Override
                    public void onActivityPaused(@NonNull Activity activity) {}
                    @Override
                    public void onActivityStopped(@NonNull Activity activity) {}
                    @Override
                    public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                            @NonNull Bundle outState) {}
                    @Override
                    public void onActivityDestroyed(@NonNull Activity activity) {
                        sActivities.remove(activity);
                    }
                });
    }

    public static void apply(@NonNull AppCompatActivity activity) {
        sActivities.add(activity);
        activity.getDelegate().setLocalNightMode(getNightMode(activity));
    }

    public static void sync() {
        for (AppCompatActivity activity : sActivities) {
            int localNightMode = activity.getDelegate().getLocalNightMode();
            int nightMode = getNightMode(activity);
            if (localNightMode != nightMode) {
                if (activity instanceof OnNightModeChangedListener) {
                    ((OnNightModeChangedListener) activity).onNightModeChangedFromHelper(nightMode);
               } else {
                    ActivityCompat.recreate(activity);
                }
            }
        }
    }

    private static int getNightMode(@NonNull AppCompatActivity activity) {
        int nightMode = SettingsLiveDatas.NIGHT_MODE.getValue().getValue();
        return AppCompatDelegateCompat.mapNightMode(activity.getDelegate(), nightMode);
    }

    public interface OnNightModeChangedListener {
        void onNightModeChangedFromHelper(int nightMode);
    }
}
