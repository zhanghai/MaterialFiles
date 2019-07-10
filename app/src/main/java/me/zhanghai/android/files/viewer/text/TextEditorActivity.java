/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.AppActivity;
import me.zhanghai.android.files.util.FragmentUtils;

public class TextEditorActivity extends AppActivity {

    private TextEditorFragment mTextEditorFragment;

    @NonNull
    public static Intent newIntent(@NonNull Path path, @NonNull Context context) {
        Intent intent = new Intent(context, TextEditorActivity.class);
        TextEditorFragment.putArguments(intent, path);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mTextEditorFragment = TextEditorFragment.newInstance(getIntent());
            FragmentUtils.add(mTextEditorFragment, this, android.R.id.content);
        } else {
            mTextEditorFragment = FragmentUtils.findById(this, android.R.id.content);
        }
    }

    @Override
    public void onBackPressed() {
        if (mTextEditorFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
