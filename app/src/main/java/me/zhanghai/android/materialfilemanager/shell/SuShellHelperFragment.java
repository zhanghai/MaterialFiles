/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.shell;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class SuShellHelperFragment extends Fragment {

    private static final String FRAGMENT_TAG = SuShellHelperFragment.class.getName();

    public static SuShellHelperFragment attachToActivity(Fragment fragment) {
        return attachTo(fragment.requireActivity());
    }

    private static SuShellHelperFragment attachTo(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        SuShellHelperFragment fragment = (SuShellHelperFragment) fragmentManager
                .findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new SuShellHelperFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setUserVisibleHint(false);

        SuShell.acquire();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SuShell.release();
    }
}
