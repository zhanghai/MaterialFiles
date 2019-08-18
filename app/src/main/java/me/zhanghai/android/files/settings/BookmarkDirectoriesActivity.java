/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
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

public class BookmarkDirectoriesActivity extends AppActivity {

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, BookmarkDirectoriesActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            FragmentUtils.add(BookmarkDirectoriesFragment.newInstance(), this,
                    android.R.id.content);
        }
    }
}
