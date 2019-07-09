/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import me.zhanghai.android.files.theme.custom.CustomThemeAppCompatActivity;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.util.FragmentUtils;

public class SettingsActivity extends CustomThemeAppCompatActivity
        implements CustomThemeHelper.OnThemeChangedListener {

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            FragmentUtils.add(SettingsActivityFragment.newInstance(), this, android.R.id.content);
        }
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);

        // AppCompatDelegateImpl.updateForNightMode() calls ActivityCompat.recreate(), which may
        // call ActivityRecreator.recreate() without calling Activity.recreate(), so we cannot
        // simply override it. To work around this, we declare android:configChanges="uiMode" in our
        // manifest and manually call restart().
        restart();
    }

    @Override
    public void onThemeChanged(@StyleRes int theme) {
        // The same thing about onNightModeChanged() applies to this as well.
        restart();
    }

    private void restart() {
        finish();
        startActivity(newIntent(this));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
