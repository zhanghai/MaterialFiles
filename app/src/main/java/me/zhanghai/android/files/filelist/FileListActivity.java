/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.FragmentUtils;

public class FileListActivity extends AppCompatActivity {

    private FileListFragment mFileListFragment;

    @NonNull
    public static Intent newIntent(@Nullable Path path, @NonNull Context context) {
        Intent intent = new Intent(context, FileListActivity.class);
        FileListFragment.putArguments(intent, path);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mFileListFragment = FileListFragment.newInstance(getIntent());
            FragmentUtils.add(mFileListFragment, this, android.R.id.content);
        } else {
            mFileListFragment = FragmentUtils.findById(this, android.R.id.content);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFileListFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
