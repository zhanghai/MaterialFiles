/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.text.BidiFormatter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.format.Time;
import android.view.View;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Locale;

public class FormatUtils {

    private FormatUtils() {}

    public static String formatShortSize(long size, Context context) {
        return Formatter.formatFileSize(context, size);
    }

    /**
     * @see Formatter#formatFileSize(android.content.Context, long)
     */
    public static String formatLongSize(long size, Context context) {
        Resources resources = context.getResources();
        int com_android_internal_R_string_fileSizeSuffix = resources.getIdentifier("fileSizeSuffix",
                "string", "android");
        int com_android_internal_R_string_byteShort = resources.getIdentifier("byteShort", "string",
                "android");
        String units = context.getString(com_android_internal_R_string_byteShort);
        return bidiWrap(context, context.getString(com_android_internal_R_string_fileSizeSuffix,
                size, units));
    }

    /*
     * @see android.text.format.Formatter#localeFromContext(Context)
     */
    private static Locale localeFromContext(@NonNull Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        } else {
            //noinspection deprecation
            return configuration.locale;
        }
    }

    /*
     * @see android.text.format.Formatter#bidiWrap(Context, String)
     */
    private static String bidiWrap(@NonNull Context context, String source) {
        Locale locale = localeFromContext(context);
        if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) {
            return BidiFormatter.getInstance(true).unicodeWrap(source);
        } else {
            return source;
        }
    }

    public static String formatShortTime(Instant instant, Context context) {
        return formatShortTime(instant.toEpochMilli(), context);
    }

    /*
     * @see com.android.documentsui.base.Shared#formatTime(Context, long)
     */
    private static String formatShortTime(long time, Context context) {
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

    public static String formatLongTime(Instant instant) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
