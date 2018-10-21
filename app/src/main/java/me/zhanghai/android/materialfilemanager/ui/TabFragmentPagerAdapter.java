/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    @NonNull
    private final List<FragmentCreator> mFragmentCreatorList = new ArrayList<>();
    @NonNull
    private final List<CharSequence> mTitleList = new ArrayList<>();

    @Deprecated
    public TabFragmentPagerAdapter(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public TabFragmentPagerAdapter(@NonNull FragmentActivity activity) {
        //noinspection deprecation
        this(activity.getSupportFragmentManager());
    }

    public TabFragmentPagerAdapter(@NonNull Fragment fragment) {
        //noinspection deprecation
        this(fragment.getChildFragmentManager());
    }

    public void addTab(@NonNull FragmentCreator fragmentCreator, @Nullable String title) {
        mFragmentCreatorList.add(fragmentCreator);
        mTitleList.add(title);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentCreatorList.get(position).createFragment();
    }

    @Override
    public int getCount() {
        return mFragmentCreatorList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }

    /**
     * @deprecated Use {@link #setPageTitle(TabLayout, int, CharSequence)} instead.
     */
    public void setPageTitle(int position, @Nullable CharSequence title) {
        mTitleList.set(position, title);
    }

    public void setPageTitle(@NonNull TabLayout tabLayout, int position,
                             @Nullable CharSequence title) {
        //noinspection deprecation
        setPageTitle(position, title);
        if (position < tabLayout.getTabCount()) {
            tabLayout.getTabAt(position).setText(title);
        }
    }

    public interface FragmentCreator {
        @NonNull
        Fragment createFragment();
    }
}
