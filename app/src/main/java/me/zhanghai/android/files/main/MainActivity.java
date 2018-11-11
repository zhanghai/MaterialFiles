/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.filesystem.Files;
import me.zhanghai.android.files.util.FragmentUtils;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_PREFIX = MainActivity.class.getName() + '.';

    private static final String EXTRA_FILE_URI = KEY_PREFIX + "FILE_URI";

    @Nullable
    private Uri mExtraFileUri;

    @NonNull
    private MainFragment mMainFragment;

    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @NonNull
    public static Intent makeIntent(@NonNull File file, @NonNull Context context) {
        return makeIntent(context)
                .putExtra(EXTRA_FILE_URI, file.getUri());
    }

    public static void putFileExtra(@NonNull Intent intent, @NonNull File file) {
        intent.putExtra(EXTRA_FILE_URI, file.getUri());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFileUri = getIntent().getParcelableExtra(EXTRA_FILE_URI);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mMainFragment = MainFragment.newInstance(getFile());
            FragmentUtils.add(mMainFragment, this, android.R.id.content);
        } else {
            mMainFragment = FragmentUtils.findById(this, android.R.id.content);
        }
    }

    private File getFile() {

        if (mExtraFileUri != null) {
            return Files.ofUri(mExtraFileUri);
        }

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && Objects.equals(data.getScheme(), "file")) {
            String path = data.getPath();
            if (!TextUtils.isEmpty(path)) {
                return Files.ofLocalPath(path);
            }
        }

        String path = intent.getStringExtra("org.openintents.extra.ABSOLUTE_PATH");
        if (path != null) {
            return Files.ofLocalPath(path);
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
