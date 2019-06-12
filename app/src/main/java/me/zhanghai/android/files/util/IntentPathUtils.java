/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.net.URI;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.Paths;

public class IntentPathUtils {

    private static final String KEY_PREFIX = IntentPathUtils.class.getName() + '.';

    private static final String EXTRA_PATH_URI = KEY_PREFIX + "PATH_URI";

    private IntentPathUtils() {}

    @NonNull
    public static Intent putExtraPath(@NonNull Intent intent, @NonNull Path path) {
        // We cannot put Path into intent here, otherwise we will crash other apps unmarshalling it.
        return intent.putExtra(EXTRA_PATH_URI, path.toUri());
    }

    @Nullable
    public static Path getExtraPath(@NonNull Intent intent) {

        URI extraPathUri = (URI) intent.getSerializableExtra(EXTRA_PATH_URI);
        if (extraPathUri != null) {
            return Paths.get(extraPathUri);
        }

        Uri data = intent.getData();
        if (data != null && Objects.equals(data.getScheme(), "file")) {
            String path = data.getPath();
            if (!TextUtils.isEmpty(path)) {
                return Paths.get(path);
            }
        }

        String extraAbsolutePath = intent.getStringExtra("org.openintents.extra.ABSOLUTE_PATH");
        if (extraAbsolutePath != null) {
            return Paths.get(extraAbsolutePath);
        }

        return null;
    }
}
