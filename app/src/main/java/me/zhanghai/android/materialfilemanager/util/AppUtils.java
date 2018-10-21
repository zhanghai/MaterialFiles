/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.zhanghai.android.materialfilemanager.R;

public class AppUtils {

    private AppUtils() {}

    @Nullable
    public static Activity getActivityFromContext(@NonNull Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            // Can be wrapped by a TintContextWrapper, etc.
            return getActivityFromContext(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    @Nullable
    private static Field sActivityTaskDescriptionField;
    private static boolean sActivityTaskDescriptionFieldInitialized;

    @Nullable
    public static ActivityManager.TaskDescription getTaskDescription(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        if (!sActivityTaskDescriptionFieldInitialized) {
            try {
                sActivityTaskDescriptionField = Activity.class.getDeclaredField("mTaskDescription");
                sActivityTaskDescriptionField.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sActivityTaskDescriptionFieldInitialized = true;
        }
        if (sActivityTaskDescriptionField == null) {
            return null;
        }
        try {
            return (ActivityManager.TaskDescription) sActivityTaskDescriptionField.get(activity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static Method sTaskDescriptionSetLabelMethod;
    private static boolean sTaskDescriptionSetLabelMethodInitialized;

    @SuppressLint("PrivateApi")
    public static void setTaskDescriptionLabel(@NonNull Activity activity, @Nullable String label) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        ActivityManager.TaskDescription taskDescription = getTaskDescription(activity);
        if (taskDescription == null) {
            return;
        }
        if (!sTaskDescriptionSetLabelMethodInitialized) {
            try {
                sTaskDescriptionSetLabelMethod = ActivityManager.TaskDescription.class
                        .getDeclaredMethod("setLabel", String.class);
                sTaskDescriptionSetLabelMethod.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sTaskDescriptionSetLabelMethodInitialized = true;
        }
        if (sTaskDescriptionSetLabelMethod == null) {
            return;
        }
        try {
            sTaskDescriptionSetLabelMethod.invoke(taskDescription, label);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        activity.setTaskDescription(taskDescription);
    }

    @Nullable
    private static Method sTaskDescriptionSetPrimaryColorMethod;
    private static boolean sTaskDescriptionSetPrimaryColorMethodInitialized;

    @SuppressLint("PrivateApi")
    public static void setTaskDescriptionPrimaryColor(@NonNull Activity activity,
                                                      @ColorInt int primaryColor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        ActivityManager.TaskDescription taskDescription = getTaskDescription(activity);
        if (taskDescription == null) {
            return;
        }
        if (!sTaskDescriptionSetPrimaryColorMethodInitialized) {
            try {
                sTaskDescriptionSetPrimaryColorMethod = ActivityManager.TaskDescription.class
                        .getDeclaredMethod("setPrimaryColor", int.class);
                sTaskDescriptionSetPrimaryColorMethod.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sTaskDescriptionSetPrimaryColorMethodInitialized = true;
        }
        if (sTaskDescriptionSetPrimaryColorMethod == null) {
            return;
        }
        try {
            sTaskDescriptionSetPrimaryColorMethod.invoke(taskDescription, primaryColor);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        activity.setTaskDescription(taskDescription);
    }

    public static boolean isIntentHandled(@NonNull Intent intent, @NonNull Context context) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    // @see http://developer.android.com/training/implementing-navigation/ancestral.html#NavigateUp
    public static void navigateUp(@NonNull Activity activity, @Nullable Bundle extras) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        if (upIntent != null) {
            if (extras != null) {
                upIntent.putExtras(extras);
            }
            if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(activity)
                        // Add all of this activity's parents to the back stack.
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent.
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                // According to http://stackoverflow.com/a/14792752/2420519
                //NavUtils.navigateUpTo(activity, upIntent);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(upIntent);
            }
        }
        activity.finish();
    }

    public static void navigateUp(@NonNull Activity activity) {
        navigateUp(activity, null);
    }

    @NonNull
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            sMainHandler.post(runnable);
        }
    }

    public static boolean startActivity(@NonNull Intent intent, @NonNull Context context) {
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, context);
            return false;
        }
    }

    public static boolean startActivityForResult(@NonNull Intent intent, int requestCode,
                                                 @NonNull Activity activity) {
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, activity);
            return false;
        }
    }

    public static void startActivityWithChooser(@NonNull Intent intent, @NonNull Context context) {
        context.startActivity(IntentUtils.withChooser(intent));
    }

    public static void startActivityForResultWithChooser(@NonNull Intent intent, int requestCode,
                                                         @NonNull Activity activity) {
        activity.startActivityForResult(IntentUtils.withChooser(intent), requestCode);
    }
}
