/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;

import java.util.HashMap;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.settings.SettingsLiveDatas;

public class NightModeHelper {

    // AppCompatDelegateImpl.updateForNightMode() gets the current night mode from Resources, whose
    // state can be shared by activities and thus cannot reflect the current night mode correctly.
    // So we keep track of the night mode upon Activity instead, which won't change once the
    // activity is created.
    private static final Map<Activity, Integer> sActivityNightModeMap = new HashMap<>();

    private NightModeHelper() {}

    public static void setup(Application application) {
        syncDefaultNightMode();
        application.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        sActivityNightModeMap.put(activity,
                                AppCompatDelegate.getDefaultNightMode());
                    }
                    @Override
                    public void onActivityStarted(Activity activity) {
                        applyDayNight(activity);
                    }
                    @Override
                    public void onActivityResumed(Activity activity) {}
                    @Override
                    public void onActivityPaused(Activity activity) {}
                    @Override
                    public void onActivityStopped(Activity activity) {}
                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        sActivityNightModeMap.remove(activity);
                    }
                });
    }

    public static void syncDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(SettingsLiveDatas.NIGHT_MODE.getValue().getValue());
    }

    public static void applyDayNight(Activity activity) {
        if (sActivityNightModeMap.get(activity) != AppCompatDelegate.getDefaultNightMode()) {
            ResourcesFlusher.flush(activity.getResources());
            activity.recreate();
        }
    }
}
