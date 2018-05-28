/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.main.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            FragmentUtils.add(MainFragment.newInstance(), this, android.R.id.content);
        }
    }
}
