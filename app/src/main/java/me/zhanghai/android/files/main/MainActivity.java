/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;

public class MainActivity extends AppCompatActivity {

    @NonNull
    private MainFragment mMainFragment;

    @NonNull
    public static Intent makeIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @NonNull
    public static Intent makeIntent(@NonNull Path path, @NonNull Context context) {
        return IntentPathUtils.putExtraPath(makeIntent(context), path);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            Path path = IntentPathUtils.getExtraPath(getIntent());
            mMainFragment = MainFragment.newInstance(path);
            FragmentUtils.add(mMainFragment, this, android.R.id.content);
        } else {
            mMainFragment = FragmentUtils.findById(this, android.R.id.content);
        }
    }

    @Override
    public void onBackPressed() {
        if (mMainFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
