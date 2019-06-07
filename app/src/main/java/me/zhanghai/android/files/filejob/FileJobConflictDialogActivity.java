/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.util.FragmentUtils;

public class FileJobConflictDialogActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = FileJobConflictDialogFragment.class.getName();

    private FileJobConflictDialogFragment mFragment;

    @NonNull
    public static Intent makeIntent(@NonNull FileItem sourceFile, @NonNull FileItem targetFile,
                                    @NonNull FileJobs.Base.CopyMoveType type,
                                    @NonNull FileJobConflictDialogFragment.Listener listener,
                                    @NonNull Context context) {
        Intent intent = new Intent(context, FileJobConflictDialogActivity.class);
        FileJobConflictDialogFragment.putArguments(intent, sourceFile, targetFile, type, listener);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mFragment = FileJobConflictDialogFragment.newInstance(getIntent());
            FragmentUtils.add(mFragment, this, FRAGMENT_TAG);
        } else {
            mFragment = FragmentUtils.findByTag(this, FRAGMENT_TAG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            mFragment.onFinish();
        }
    }
}
