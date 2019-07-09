/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;

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
                // According to http://stackoverflow.com/a/14792752
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

    public static boolean startActivity(@NonNull Intent intent, @NonNull Fragment fragment) {
        try {
            fragment.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, fragment.requireContext());
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

    public static boolean startActivityForResult(@NonNull Intent intent, int requestCode,
                                                 @NonNull Fragment fragment) {
        try {
            fragment.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            ToastUtils.show(R.string.activity_not_found, fragment.requireContext());
            return false;
        }
    }

    public static void startActivityWithChooser(@NonNull Intent intent, @NonNull Context context) {
        context.startActivity(IntentUtils.withChooser(intent));
    }

    public static void startActivityWithChooser(@NonNull Intent intent, @NonNull Fragment fragment) {
        fragment.startActivity(IntentUtils.withChooser(intent));
    }

    public static void startActivityForResultWithChooser(@NonNull Intent intent, int requestCode,
                                                         @NonNull Activity activity) {
        activity.startActivityForResult(IntentUtils.withChooser(intent), requestCode);
    }

    public static void startActivityForResultWithChooser(@NonNull Intent intent, int requestCode,
                                                         @NonNull Fragment fragment) {
        fragment.startActivityForResult(IntentUtils.withChooser(intent), requestCode);
    }
}
