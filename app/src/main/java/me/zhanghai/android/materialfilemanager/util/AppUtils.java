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
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.zhanghai.android.materialfilemanager.R;

public class AppUtils {

    private AppUtils() {}

    @Nullable
    public static Activity getActivityFromContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            // Can be wrapped by a TintContextWrapper, etc.
            return getActivityFromContext(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    private static Field sActivityTaskDescriptionField;

    public static ActivityManager.TaskDescription getTaskDescription(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        if (sActivityTaskDescriptionField == null) {
            try {
                sActivityTaskDescriptionField = Activity.class.getDeclaredField("mTaskDescription");
                sActivityTaskDescriptionField.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            return (ActivityManager.TaskDescription) sActivityTaskDescriptionField.get(activity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method sTaskDescriptionSetLabelMethod;

    @SuppressLint("PrivateApi")
    public static void setTaskDescriptionLabel(Activity activity, String label) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        ActivityManager.TaskDescription taskDescription = getTaskDescription(activity);
        if (taskDescription == null) {
            return;
        }
        if (sTaskDescriptionSetLabelMethod == null) {
            try {
                sTaskDescriptionSetLabelMethod = ActivityManager.TaskDescription.class
                        .getDeclaredMethod("setLabel", String.class);
                sTaskDescriptionSetLabelMethod.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            sTaskDescriptionSetLabelMethod.invoke(taskDescription, label);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        activity.setTaskDescription(taskDescription);
    }

    private static Method sTaskDescriptionSetPrimaryColorMethod;

    @SuppressLint("PrivateApi")
    public static void setTaskDescriptionPrimaryColor(Activity activity, int primaryColor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        ActivityManager.TaskDescription taskDescription = getTaskDescription(activity);
        if (taskDescription == null) {
            return;
        }
        if (sTaskDescriptionSetPrimaryColorMethod == null) {
            try {
                sTaskDescriptionSetPrimaryColorMethod = ActivityManager.TaskDescription.class
                        .getDeclaredMethod("setPrimaryColor", int.class);
                sTaskDescriptionSetPrimaryColorMethod.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            sTaskDescriptionSetPrimaryColorMethod.invoke(taskDescription, primaryColor);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        activity.setTaskDescription(taskDescription);
    }

    public static boolean isIntentHandled(Intent intent, Context context) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    // From http://developer.android.com/training/implementing-navigation/ancestral.html#NavigateUp .
    public static void navigateUp(Activity activity, Bundle extras) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        if (upIntent != null) {
            if (extras != null) {
                upIntent.putExtras(extras);
            }
            if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
                // This activity is NOT part of this app's task, so ofPath a new task
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

    public static void navigateUp(Activity activity) {
        navigateUp(activity, null);
    }

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            sMainHandler.post(runnable);
        }
    }

    public static boolean startActivity(Intent intent, Context context) {
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, context);
            return false;
        }
    }

    public static boolean startActivityForResult(Intent intent, int requestCode, Activity activity) {
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, activity);
            return false;
        }
    }

    public static void startActivityWithChooser(Intent intent, Context context) {
        context.startActivity(IntentUtils.withChooser(intent));
    }

    public static void startActivityForResultWithChooser(Intent intent, int requestCode,
                                                            Activity activity) {
        activity.startActivityForResult(IntentUtils.withChooser(intent), requestCode);
    }
}
