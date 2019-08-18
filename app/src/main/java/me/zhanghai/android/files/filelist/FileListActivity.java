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
import java8.nio.file.Path;
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;

public class FileListActivity extends AppActivity {

    private FileListFragment mFileListFragment;

    @NonNull
    public static Intent newViewIntent(@Nullable Path path, @NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setClass(context, FileListActivity.class);
        if (path != null) {
            IntentPathUtils.putExtraPath(intent, path);
        }
        return intent;
    }

    @NonNull
    public static Intent newPickDirectoryIntent(@Nullable Path initialPath,
                                                @NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .setClass(context, FileListActivity.class);
        if (initialPath != null) {
            IntentPathUtils.putExtraPath(intent, initialPath);
        }
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
