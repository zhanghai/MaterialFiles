/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.format.Time;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.R;

public class FormatUtils {

    private FormatUtils() {}

    /*
     * @see android.text.format.Formatter#formatBytes(Resources, long, int)
     */
    public static boolean isHumanReadableSizeInBytes(long size) {
        return size <= 900;
    }

    @NonNull
    public static String formatHumanReadableSize(long size, @NonNull Context context) {
        return Formatter.formatFileSize(context, size);
    }

    @NonNull
    public static String formatSizeInBytes(long size, @NonNull Context context) {
        // HACK
        int quantity = (int) size;
        return context.getResources().getQuantityString(R.plurals.size_in_bytes_format, quantity,
                size);
    }

    @NonNull
    public static String formatShortTime(@NonNull Instant instant, @NonNull Context context) {
        return formatShortTime(instant.toEpochMilli(), context);
    }

    /*
     * @see com.android.documentsui.base.Shared#formatTime(Context, long)
     */
    @NonNull
    private static String formatShortTime(long time, @NonNull Context context) {
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

    @NonNull
    public static String formatLongTime(@NonNull Instant instant) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
