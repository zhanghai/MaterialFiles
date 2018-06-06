/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class MainActivity extends AppCompatActivity {

    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            mMainFragment = MainFragment.newInstance();
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
