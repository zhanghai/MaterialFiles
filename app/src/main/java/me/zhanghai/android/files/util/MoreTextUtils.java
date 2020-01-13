/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MoreTextUtils {

    private MoreTextUtils() {}

    /**
     * @deprecated Use {@link TextUtils#equals(CharSequence, CharSequence)} instead.
     */
    public static boolean equalsAny(@Nullable CharSequence text, @Nullable CharSequence text1) {
        return TextUtils.equals(text, text1);
    }

    public static boolean equalsAny(@Nullable CharSequence text, @Nullable CharSequence text1,
                                    @Nullable CharSequence text2) {
        return TextUtils.equals(text, text1) || TextUtils.equals(text, text2);
    }

    public static boolean equalsAny(@Nullable CharSequence text, @Nullable CharSequence text1,
                                    @Nullable CharSequence text2, @Nullable CharSequence text3) {
        return TextUtils.equals(text, text1) || TextUtils.equals(text, text2)
                || TextUtils.equals(text, text3);
    }

    public static boolean equalsAny(@Nullable CharSequence text, CharSequence... array) {
        for (CharSequence element : array) {
            if (TextUtils.equals(text, element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(@Nullable String text, @NonNull CharSequence text1) {
        return text != null && text.contains(text1);
    }

    /**
     * @deprecated Use {@link #contains(String, CharSequence)} instead.
     */
    public static boolean containsAny(@Nullable String text, @NonNull CharSequence text1) {
        return contains(text, text1);
    }

    public static boolean containsAny(@Nullable String text, @NonNull CharSequence text1,
                                      @NonNull CharSequence text2) {
        return contains(text, text1) || contains(text, text2);
    }

    public static boolean containsAny(@Nullable String text, @NonNull CharSequence text1,
                                      @NonNull CharSequence text2, @NonNull CharSequence text3) {
        return contains(text, text1) || contains(text, text2) || contains(text, text3);
    }

    public static boolean containsAny(@Nullable String text, CharSequence... array) {
        for (CharSequence element : array) {
            if (contains(text, element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsIgnoreCase(@NonNull String text, @NonNull String other) {
        int otherLength = other.length();
        if (otherLength == 0) {
            return true;
        }
        for (int i = 0, max = text.length() - otherLength; i <= max; ++i) {
            if (text.regionMatches(true, i, other, 0, otherLength)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(@Nullable String text, @NonNull String prefix) {
        return text != null && text.startsWith(prefix);
    }

    public static boolean endsWith(@Nullable String text, @NonNull String prefix) {
        return text != null && text.endsWith(prefix);
    }

    @Nullable
    public static CharSequence emptyToNull(@Nullable CharSequence text) {
        return text != null && text.length() == 0 ? null : text;
    }

    @NonNull
    public static CharSequence nullToEmpty(@Nullable CharSequence text) {
        return text == null ? "" : text;
    }

    @Nullable
    public static String emptyToNull(@Nullable String text) {
        return text != null && text.isEmpty() ? null : text;
    }

    @NonNull
    public static String nullToEmpty(@Nullable String text) {
        return text == null ? "" : text;
    }
}
