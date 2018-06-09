/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class TimeUtils {

    /*
     * @see com.android.documentsui.base.Shared#formatTime(Context, long)
     */
    public static String formatTime(long time, Context context) {
        Time then = new Time();
        then.set(time);
        Time now = new Time();
        now.setToNow();
        int flags = DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_NO_MIDNIGHT
                | DateUtils.FORMAT_ABBREV_ALL;
        if (then.year != now.year) {
            flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            flags |= DateUtils.FORMAT_SHOW_TIME;
        }
        return DateUtils.formatDateTime(context, time, flags);
    }
}
