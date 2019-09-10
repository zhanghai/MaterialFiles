/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.fileproperties.basic.FilePropertiesBasicTabFragment;
import me.zhanghai.android.files.fileproperties.permissions.FilePropertiesPermissionsTabFragment;
import me.zhanghai.android.files.ui.TabFragmentPagerAdapter;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class FilePropertiesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FileItem";

    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @NonNull
    private View mView;

    @NonNull
    private TabFragmentPagerAdapter mTabAdapter;

    @NonNull
    private FileItem mExtraFile;

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public FilePropertiesDialogFragment() {}

    @NonNull
    public static FilePropertiesDialogFragment newInstance(@NonNull FileItem fileItem) {
        //noinspection deprecation
        FilePropertiesDialogFragment fragment = new FilePropertiesDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, fileItem);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = getString(R.string.file_properties_title_format, FileUtils.getName(
                mExtraFile));
        AlertDialog.Builder builder = AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setTitle(title);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTabAdapter = new TabFragmentPagerAdapter(this);
        mTabAdapter.addTab(() -> FilePropertiesBasicTabFragment.newInstance(mExtraFile), getString(
                R.string.file_properties_basic));
        if (FilePropertiesPermissionsTabFragment.isAvailable(mExtraFile)) {
            mTabAdapter.addTab(() -> FilePropertiesPermissionsTabFragment.newInstance(mExtraFile),
                    getString(R.string.file_properties_permissions));
        }
        mViewPager.setOffscreenPageLimit(mTabAdapter.getCount() - 1);
        mViewPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public static void show(@NonNull FileItem fileItem, @NonNull Fragment fragment) {
        FilePropertiesDialogFragment.newInstance(fileItem)
                .show(fragment.getChildFragmentManager(), null);
    }
}
