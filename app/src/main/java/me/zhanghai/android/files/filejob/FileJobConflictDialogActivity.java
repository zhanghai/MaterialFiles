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
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.util.FragmentUtils;

public class FileJobConflictDialogActivity extends AppActivity {

    private static final String FRAGMENT_TAG = FileJobConflictDialogFragment.class.getName();

    private FileJobConflictDialogFragment mFragment;

    @NonNull
    public static Intent newIntent(@NonNull FileItem sourceFile, @NonNull FileItem targetFile,
                                   @NonNull FileJobs.Base.CopyMoveType type,
                                   @NonNull FileJobConflictDialogFragment.Listener listener,
                                   @NonNull Context context) {
        Intent intent = new Intent(context, FileJobConflictDialogActivity.class);
        FileJobConflictDialogFragment.putArguments(intent, sourceFile, targetFile, type, listener);
        return intent;
    }

    @NonNull
    public static String getTitle(@NonNull FileItem sourceFile, @NonNull FileItem targetFile,
                                  @NonNull Context context) {
        return FileJobConflictDialogFragment.getTitle(sourceFile, targetFile, context);
    }

    @NonNull
    public static String getMessage(@NonNull FileItem sourceFile, @NonNull FileItem targetFile,
                                    @NonNull FileJobs.Base.CopyMoveType type,
                                    @NonNull Context context) {
        return FileJobConflictDialogFragment.getMessage(sourceFile, targetFile, type, context);
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
