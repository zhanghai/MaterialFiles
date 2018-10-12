/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filelist.FileListFragment;
import me.zhanghai.android.materialfilemanager.navigation.NavigationFragment;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class MainFragment extends Fragment implements NavigationFragment.MainListener {

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    private NavigationFragment mNavigationFragment;
    private FileListFragment mFileListFragment;

    public static MainFragment newInstance() {
        //noinspection deprecation
        return new MainFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mNavigationFragment = FragmentUtils.findById(this, R.id.navigation_fragment);
        mFileListFragment = FragmentUtils.findById(this, R.id.file_list_fragment);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNavigationFragment.setListeners(this, mFileListFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    public boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return true;
        }
        return mFileListFragment.onBackPressed();
    }
}
