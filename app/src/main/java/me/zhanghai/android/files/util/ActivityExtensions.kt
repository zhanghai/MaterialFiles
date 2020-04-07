/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder

// @see http://developer.android.com/training/implementing-navigation/ancestral.html#NavigateUp
fun Activity.navigateUp(extras: Bundle? = null) {
    val upIntent = NavUtils.getParentActivityIntent(this)
    if (upIntent != null) {
        if (extras != null) {
            upIntent.putExtras(extras)
        }
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack.
                .addNextIntentWithParentStack(upIntent) // Navigate up to the closest parent.
                .startActivities()
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            // According to http://stackoverflow.com/a/14792752
            //NavUtils.navigateUpTo(activity, upIntent);
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivitySafe(upIntent)
        }
    }
    finish()
}
