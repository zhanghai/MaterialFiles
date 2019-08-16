/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.util.FragmentUtils;

public class StandardDirectoriesActivity extends AppActivity {

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, StandardDirectoriesActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            FragmentUtils.add(StandardDirectoriesFragment.newInstance(), this,
                    android.R.id.content);
        }
    }
}
