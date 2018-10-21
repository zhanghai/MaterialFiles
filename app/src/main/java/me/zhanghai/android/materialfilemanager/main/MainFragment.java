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
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.navigation.NavigationFragment;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class MainFragment extends Fragment implements NavigationFragment.MainListener {

    private static final String KEY_PREFIX = MainFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @Nullable
    private File mExtraFile;

    @NonNull
    private NavigationFragment mNavigationFragment;
    @NonNull
    private FileListFragment mFileListFragment;

    @NonNull
    public static MainFragment newInstance(@Nullable File file) {
        //noinspection deprecation
        MainFragment fragment = new MainFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);

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

        // Add FileListFragment first so that NavigationFragment can observe its current file.
        mFileListFragment = FileListFragment.newInstance(mExtraFile);
        FragmentUtils.add(mFileListFragment, this, R.id.file_list_fragment);
        mNavigationFragment = NavigationFragment.newInstance();
        FragmentUtils.add(mNavigationFragment, this, R.id.navigation_fragment);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNavigationFragment.setListeners(this, mFileListFragment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
