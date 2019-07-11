/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.theme.night.NightModeHelper;

public abstract class AppActivity extends AppCompatActivity {

    private boolean mDelegateCreated;

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        AppCompatDelegate delegate = super.getDelegate();
        if (!mDelegateCreated) {
            mDelegateCreated = true;
            NightModeHelper.apply(this);
        }
        return delegate;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        CustomThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
    }
}
