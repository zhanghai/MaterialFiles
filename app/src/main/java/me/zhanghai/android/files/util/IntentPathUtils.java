/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java9.util.function.Function;
import me.zhanghai.java.functional.Functional;

public class IntentPathUtils {

    private static final String KEY_PREFIX = IntentPathUtils.class.getName() + '.';

    private static final String EXTRA_PATH_URI = KEY_PREFIX + "PATH_URI";

    private static final String EXTRA_PATH_URI_LIST = KEY_PREFIX + "PATH_URI_LIST";

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

        Uri extraInitialUri = intent.getParcelableExtra(DocumentsContract.EXTRA_INITIAL_URI);
        // TODO: Support DocumentsProvider Uri?
        if (extraInitialUri != null && Objects.equals(extraInitialUri.getScheme(), "file")) {
            String path = extraInitialUri.getPath();
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

    @NonNull
    public static Intent putExtraPathList(@NonNull Intent intent, @NonNull List<Path> paths) {
        // We cannot put Path into intent here, otherwise we will crash other apps unmarshalling it.
        ArrayList<URI> pathUris = Functional.map(paths, Path::toUri);
        return intent.putExtra(EXTRA_PATH_URI_LIST, pathUris);
    }

    @NonNull
    public static List<Path> getExtraPathList(@NonNull Intent intent) {

        //noinspection unchecked
        List<URI> extraPathUris = (List<URI>) intent.getSerializableExtra(EXTRA_PATH_URI_LIST);
        if (extraPathUris != null) {
            return Functional.map(extraPathUris, (Function<URI, Path>) Paths::get);
        }

        Path extraPath = getExtraPath(intent);
        return CollectionUtils.singletonListOrEmpty(extraPath);
    }
}
