/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.night;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDelegateCompat;
import me.zhanghai.android.files.settings.Settings;

// We take over the activity creation when setting the default night mode from AppCompat so that:
// 1. We can recreate all activities upon change, instead of only started activities.
// 2. We can have custom handling of the change, instead of being forced to either recreate or
//    update resources configuration which is shared among activities.
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
        activity.getDelegate().setLocalNightMode(getNightMode());
    }

    public static void sync() {
        for (AppCompatActivity activity : sActivities) {
            int nightMode = getNightMode();
            if (activity instanceof OnNightModeChangedListener) {
                int localNightMode = activity.getDelegate().getLocalNightMode();
                if (getUiModeNight(localNightMode, activity) != getUiModeNight(nightMode,
                        activity)) {
                    ((OnNightModeChangedListener) activity).onNightModeChangedFromHelper(nightMode);
                }
            } else {
                activity.getDelegate().setLocalNightMode(nightMode);
            }
        }
    }

    private static int getNightMode() {
        return Settings.NIGHT_MODE.getValue().getValue();
    }

    /*
     * @see androidx.appcompat.app.AppCompatDelegateImpl#updateForNightMode(int, boolean)
     */
    private static int getUiModeNight(int nightMode, @NonNull AppCompatActivity activity) {
        nightMode = AppCompatDelegateCompat.mapNightMode(activity.getDelegate(), nightMode);
        switch (nightMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                return Configuration.UI_MODE_NIGHT_YES;
            case AppCompatDelegate.MODE_NIGHT_NO:
                return Configuration.UI_MODE_NIGHT_NO;
            default: {
                return activity.getApplicationContext().getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
            }
        }
    }

    public interface OnNightModeChangedListener {
        void onNightModeChangedFromHelper(int nightMode);
    }
}
