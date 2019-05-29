/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.net.URI;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import me.zhanghai.android.files.util.FragmentUtils;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_PREFIX = MainActivity.class.getName() + '.';

    private static final String EXTRA_PATH_URI = KEY_PREFIX + "PATH_URI";

    @Nullable
    private Path mExtraPath;

    @NonNull
    private MainFragment mMainFragment;

    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @NonNull
    public static Intent makeIntent(@NonNull Path path, @NonNull Context context) {
        return makeIntent(context)
                .putExtra(EXTRA_PATH_URI, path.toUri());
    }

    public static void putPathExtra(@NonNull Intent intent, @NonNull Path path) {
        // We cannot put Path into intent here, otherwise we will crash other apps unmarshalling it.
        intent.putExtra(EXTRA_PATH_URI, path.toUri());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        URI extraPathUri = (URI) getIntent().getSerializableExtra(EXTRA_PATH_URI);
        mExtraPath = extraPathUri != null ? Paths.get(extraPathUri) : null;

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mMainFragment = MainFragment.newInstance(getPath());
            FragmentUtils.add(mMainFragment, this, android.R.id.content);
        } else {
            mMainFragment = FragmentUtils.findById(this, android.R.id.content);
        }
    }

    @Nullable
    private Path getPath() {

        if (mExtraPath != null) {
            return mExtraPath;
        }

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && Objects.equals(data.getScheme(), "file")) {
            String path = data.getPath();
            if (!TextUtils.isEmpty(path)) {
                return Paths.get(path);
            }
        }

        String path = intent.getStringExtra("org.openintents.extra.ABSOLUTE_PATH");
        if (path != null) {
            return Paths.get(path);
        }

        return null;
    }

    @Override
    public void onBackPressed() {
        if (mMainFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
