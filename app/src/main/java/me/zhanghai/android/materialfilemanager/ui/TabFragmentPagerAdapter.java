/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<FragmentCreator> mFragmentCreatorList = new ArrayList<>();
    private List<CharSequence> mTitleList = new ArrayList<>();

    @Deprecated
    public TabFragmentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public TabFragmentPagerAdapter(FragmentActivity activity) {
        //noinspection deprecation
        this(activity.getSupportFragmentManager());
    }

    public TabFragmentPagerAdapter(Fragment fragment) {
        //noinspection deprecation
        this(fragment.getChildFragmentManager());
    }

    public void addTab(FragmentCreator fragmentCreator, String title) {
        mFragmentCreatorList.add(fragmentCreator);
        mTitleList.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentCreatorList.get(position).createFragment();
    }

    @Override
    public int getCount() {
        return mFragmentCreatorList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }

    /**
     * @deprecated Use {@link #setPageTitle(TabLayout, int, CharSequence)} instead.
     */
    public void setPageTitle(int position, CharSequence title) {
        mTitleList.set(position, title);
    }

    public void setPageTitle(TabLayout tabLayout, int position, CharSequence title) {
        //noinspection deprecation
        setPageTitle(position, title);
        if (position < tabLayout.getTabCount()) {
            tabLayout.getTabAt(position).setText(title);
        }
    }

    public interface FragmentCreator {
        Fragment createFragment();
    }
}
