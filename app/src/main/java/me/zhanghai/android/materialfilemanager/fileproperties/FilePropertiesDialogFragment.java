/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.fileproperties;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.ui.TabFragmentPagerAdapter;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class FilePropertiesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(android.R.id.button1)
    Button mPositiveButton;
    @BindView(android.R.id.button2)
    Button mNegativeButton;
    @BindView(android.R.id.button3)
    Button mNeutralButton;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = (AppCompatDialog) super.onCreateDialog(savedInstanceState);
        // We are using a custom title, as in AlertDialog.
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_properties_dialog_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
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
        mPositiveButton.setText(android.R.string.ok);
        mPositiveButton.setVisibility(View.VISIBLE);
        mPositiveButton.setOnClickListener(view -> dismiss());
        mNegativeButton.setVisibility(View.GONE);
        mNeutralButton.setVisibility(View.GONE);
    }

    public static void show(File file, Fragment fragment) {
        FilePropertiesDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }
}
