/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.util.FragmentUtils;

public class OpenFileAsDialogActivity extends AppActivity {

    private static final String FRAGMENT_TAG = OpenFileAsDialogFragment.class.getName();

    @NonNull
    public static Intent newIntent(@NonNull Path path, @NonNull Context context) {
        Intent intent = new Intent(context, OpenFileAsDialogActivity.class);
        OpenFileAsDialogFragment.putArguments(intent, path);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            OpenFileAsDialogFragment fragment = OpenFileAsDialogFragment.newInstance(getIntent());
            FragmentUtils.add(fragment, this, FRAGMENT_TAG);
        }
    }
}
