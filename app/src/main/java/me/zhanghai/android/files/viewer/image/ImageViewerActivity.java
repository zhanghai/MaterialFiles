/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.FragmentUtils;

public class ImageViewerActivity extends AppCompatActivity {

    @NonNull
    public static Intent newIntent(@NonNull List<Path> paths, int position, @NonNull Context context) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        ImageViewerFragment.putArguments(intent, paths, position);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            ImageViewerFragment fragment = ImageViewerFragment.newInstance(getIntent());
            FragmentUtils.add(fragment, this, android.R.id.content);
        }
    }
}
