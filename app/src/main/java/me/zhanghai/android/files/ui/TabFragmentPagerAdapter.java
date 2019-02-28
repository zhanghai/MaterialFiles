/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
