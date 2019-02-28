/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import me.zhanghai.android.files.R;

public class ClipboardUtils {

    private static final int TOAST_COPIED_TEXT_MAX_LENGTH = 40;

    private ClipboardUtils() {}

    @NonNull
    private static ClipboardManager getClipboardManager(@NonNull Context context) {
        return ContextCompat.getSystemService(context, ClipboardManager.class);
    }

    @Nullable
    public static CharSequence readText(@NonNull Context context) {
        ClipData clipData = getClipboardManager(context).getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        return clipData.getItemAt(0).coerceToText(context);
    }

    public static void copyText(@Nullable CharSequence label, @NonNull CharSequence text,
                                @NonNull Context context) {
        ClipData clipData = ClipData.newPlainText(label, text);
        getClipboardManager(context).setPrimaryClip(clipData);
        showToast(text, context);
    }

    public static void copyText(@Nullable CharSequence text, @NonNull Context context) {
        copyText(null, text, context);
    }

    private static void showToast(@NonNull CharSequence copiedText, @NonNull Context context) {
        boolean ellipsized = false;
        if (copiedText.length() > TOAST_COPIED_TEXT_MAX_LENGTH) {
            copiedText = copiedText.subSequence(0, TOAST_COPIED_TEXT_MAX_LENGTH);
            ellipsized = true;
        }
        int indexOfFirstNewline = TextUtils.indexOf(copiedText, '\n');
        if (indexOfFirstNewline != -1) {
            int indexOfSecondNewline = TextUtils.indexOf(copiedText, '\n', indexOfFirstNewline + 1);
            if (indexOfSecondNewline != -1) {
                copiedText = copiedText.subSequence(0, indexOfSecondNewline);
                ellipsized = true;
            }
        }
        if (ellipsized) {
            copiedText = copiedText.toString() + '\u2026';
        }
        ToastUtils.show(context.getString(R.string.copied_to_clipboard_format, copiedText),
                context);
    }
}
