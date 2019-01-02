/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.main;

import android.os.Bundle;
import android.os.Parcelable;
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

    private static final String KEY_PREFIX = MainFragment.class.getName() + '.';

    private static final String EXTRA_PATH = KEY_PREFIX + "PATH";

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @Nullable
    private Path mExtraPath;

    @NonNull
    private NavigationFragment mNavigationFragment;
    @NonNull
    private FileListFragment mFileListFragment;

    @NonNull
    public static MainFragment newInstance(@Nullable Path path) {
        //noinspection deprecation
        MainFragment fragment = new MainFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_PATH, (Parcelable) path);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Path)} instead.
     */
    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraPath = getArguments().getParcelable(EXTRA_PATH);

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

        if (savedInstanceState == null) {
            mNavigationFragment = NavigationFragment.newInstance();
            mFileListFragment = FileListFragment.newInstance(mExtraPath);
            // Add FileListFragment first so that NavigationFragment can observe its current file.
            FragmentUtils.add(mFileListFragment, this, R.id.file_list_fragment);
            FragmentUtils.add(mNavigationFragment, this, R.id.navigation_fragment);
        } else {
            mFileListFragment = FragmentUtils.findById(this, R.id.file_list_fragment);
            mNavigationFragment = FragmentUtils.findById(this, R.id.navigation_fragment);
        }
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
