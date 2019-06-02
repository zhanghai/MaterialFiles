/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;

public class TextEditorActivity extends AppCompatActivity {

    private TextEditorFragment mTextEditorFragment;

    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, TextEditorActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            Path path = IntentPathUtils.getExtraPath(getIntent());
            if (path == null) {
                // TODO: Show a toast.
                finish();
                return;
            }
            mTextEditorFragment = TextEditorFragment.newInstance(path);
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
