/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.about;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.ui.LicensesDialogFragment;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.IntentUtils;

public class AboutFragment extends Fragment {

    private static final Uri GITHUB_URI = Uri.parse(
            "https://github.com/DreaminginCodeZH/MaterialFileManager");

    private static final Uri AUTHOR_GITHUB_URI = Uri.parse("https://github.com/DreaminginCodeZH");

    private static final Uri AUTHOR_GOOGLE_PLUS_URI = Uri.parse(
            "https://plus.google.com/105148560373589648355");

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.github)
    ViewGroup mGitHubLayout;
    @BindView(R.id.licenses)
    ViewGroup mLicensesLayout;
    @BindView(R.id.author_github)
    ViewGroup mAuthorGitHubLayout;
    @BindView(R.id.author_google_plus)
    ViewGroup mAuthorGooglePlusLayout;

    @NonNull
    public static AboutFragment newInstance() {
        //noinspection deprecation
        return new AboutFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public AboutFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mGitHubLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                GITHUB_URI), this));
        mLicensesLayout.setOnClickListener(view -> LicensesDialogFragment.show(this));
        mAuthorGitHubLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                AUTHOR_GITHUB_URI), this));
        mAuthorGooglePlusLayout.setOnClickListener(view -> AppUtils.startActivity(
                IntentUtils.makeView(AUTHOR_GOOGLE_PLUS_URI), this));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //activity.onSupportNavigateUp();
                activity.finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
