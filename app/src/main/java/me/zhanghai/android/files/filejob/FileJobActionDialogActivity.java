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
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.util.FragmentUtils;

public class FileJobActionDialogActivity extends AppActivity {

    private static final String FRAGMENT_TAG = FileJobActionDialogFragment.class.getName();

    private FileJobActionDialogFragment mFragment;

    @NonNull
    public static Intent newIntent(@NonNull CharSequence title, @NonNull CharSequence message,
                                   @Nullable PosixFileStore readOnlyFileStore, boolean showAll,
                                   @Nullable CharSequence positiveButtonText,
                                   @Nullable CharSequence negativeButtonText,
                                   @Nullable CharSequence neutralButtonText,
                                   @NonNull FileJobActionDialogFragment.Listener listener,
                                   @NonNull Context context) {
        Intent intent = new Intent(context, FileJobActionDialogActivity.class);
        FileJobActionDialogFragment.putArguments(intent, title, message, readOnlyFileStore, showAll,
                positiveButtonText, negativeButtonText, neutralButtonText, listener);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mFragment = FileJobActionDialogFragment.newInstance(getIntent());
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
