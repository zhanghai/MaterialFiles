/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileListFragment;
import me.zhanghai.android.files.navigation.NavigationFragment;
import me.zhanghai.android.files.util.FragmentUtils;

public class MainFragment extends Fragment implements NavigationFragment.MainListener {

    private Intent mIntent;

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @NonNull
    private NavigationFragment mNavigationFragment;
    @NonNull
    private FileListFragment mFileListFragment;

    public static void putArguments(@NonNull Intent intent, @Nullable Path path) {
        FileListFragment.putArguments(intent, path);
    }

    @NonNull
    public static MainFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        MainFragment fragment = new MainFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(Intent.EXTRA_INTENT, intent);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getArguments().getParcelable(Intent.EXTRA_INTENT);

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            mNavigationFragment = NavigationFragment.newInstance();
            mFileListFragment = FileListFragment.newInstance(mIntent);
            // Add FileListFragment first so that NavigationFragment can observe its current file.
            FragmentUtils.add(mFileListFragment, this, R.id.file_list_fragment);
            FragmentUtils.add(mNavigationFragment, this, R.id.navigation_fragment);
        } else {
            mFileListFragment = FragmentUtils.findById(this, R.id.file_list_fragment);
            mNavigationFragment = FragmentUtils.findById(this, R.id.navigation_fragment);
        }

        mNavigationFragment.setListeners(this, mFileListFragment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return mFileListFragment.onBackPressed();
    }
}
