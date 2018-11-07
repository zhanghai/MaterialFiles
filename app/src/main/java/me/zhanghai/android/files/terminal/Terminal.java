/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.terminal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.util.AppUtils;

public class Terminal {

    private Terminal() {}

    public static boolean open(@NonNull String path, @NonNull Context context) {
        return AppUtils.startActivity(makeIntent(path), context);
    }

    @NonNull
    private static Intent makeIntent(@NonNull String path) {
        return new Intent()
                .setComponent(new ComponentName("jackpal.androidterm",
                        "jackpal.androidterm.TermHere"))
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
    }
}
