/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityCompat;
import me.zhanghai.android.files.compat.MoreActivityCompat;
import me.zhanghai.android.files.compat.MoreContextCompat;
import me.zhanghai.android.files.settings.Settings;

public class CustomThemeHelper {

    private static final HashMap<Activity, Integer> sActivityBaseThemes = new HashMap<>();

    private CustomThemeHelper() {}

    public static void initialize(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(@NonNull Activity activity,
                                                  @Nullable Bundle savedInstanceState) {
                        if (!sActivityBaseThemes.containsKey(activity)) {
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
                        sActivityBaseThemes.remove(activity);
                    }
                });
    }

    public static void apply(@NonNull Activity activity) {
        int baseThemeRes = MoreContextCompat.getThemeResId(activity);
        sActivityBaseThemes.put(activity, baseThemeRes);
        int customThemeRes = getCustomTheme(baseThemeRes, activity);
        MoreActivityCompat.setTheme(activity, customThemeRes);
    }

    public static void sync() {
        for (Map.Entry<Activity, Integer> entry : sActivityBaseThemes.entrySet()) {
            Activity activity = entry.getKey();
            int baseThemeRes = entry.getValue();

            int currentThemeRes = MoreContextCompat.getThemeResId(activity);
            int customThemeRes = getCustomTheme(baseThemeRes, activity);
            if (currentThemeRes != customThemeRes) {
                if (activity instanceof OnThemeChangedListener) {
                    ((OnThemeChangedListener) activity).onThemeChanged(customThemeRes);
                } else {
                    ActivityCompat.recreate(activity);
                }
            }
        }
    }

    @StyleRes
    private static int getCustomTheme(@StyleRes int baseThemeRes, @NonNull Context context) {
        Resources resources = context.getResources();
        String baseThemeName = resources.getResourceName(baseThemeRes);
        String customThemeName;
        if (Settings.MATERIAL_DESIGN_2.getValue()) {
            customThemeName = baseThemeName + ".Md2";
        } else {
            String primaryColorEntryName = Settings.PRIMARY_COLOR.getValue()
                    .getResourceEntryName();
            String accentColorEntryName = Settings.ACCENT_COLOR.getValue()
                    .getResourceEntryName();
            customThemeName = baseThemeName + "." + primaryColorEntryName + "."
                    + accentColorEntryName;
        }
        return resources.getIdentifier(customThemeName, null, null);
    }

    public interface OnThemeChangedListener {
        void onThemeChanged(@StyleRes int theme);
    }
}
