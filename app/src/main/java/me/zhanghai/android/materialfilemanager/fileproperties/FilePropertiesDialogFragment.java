/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.fileproperties;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.ui.TabFragmentPagerAdapter;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FilePropertiesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private View mView;

    private TabFragmentPagerAdapter mTabAdapter;

    private File mFile;

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public FilePropertiesDialogFragment() {}

    public static FilePropertiesDialogFragment newInstance(File file) {
        //noinspection deprecation
        FilePropertiesDialogFragment fragment = new FilePropertiesDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), getTheme())
                .setTitle(mFile.getName());
        mView = ViewUtils.inflate(R.layout.file_properties_dialog, builder.getContext());
        ButterKnife.bind(this, mView);
        return builder.setView(mView)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    // HACK: Work around child FragmentManager requiring a view.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTabAdapter = new TabFragmentPagerAdapter(this);
        mTabAdapter.addTab(() -> FilePropertiesBasicTabFragment.newInstance(mFile), getString(
                R.string.file_properties_basic));
        mTabAdapter.addTab(() -> FilePropertiesPermissionsTabFragment.newInstance(mFile), getString(
                R.string.file_properties_permissions));
        mViewPager.setOffscreenPageLimit(mTabAdapter.getCount() - 1);
        mViewPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public static void show(File file, Fragment fragment) {
        FilePropertiesDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }
}
