/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.about;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.ui.LicensesDialogFragment;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.IntentUtils;

public class AboutFragment extends Fragment {

    private static final Uri GITHUB_URI = Uri.parse(
            "https://github.com/zhanghai/MaterialFiles");

    private static final Uri PRIVACY_POLICY_URI = Uri.parse(
            "https://github.com/zhanghai/MaterialFiles/blob/master/PRIVACY.md");

    private static final Uri AUTHOR_GITHUB_URI = Uri.parse("https://github.com/zhanghai");

    private static final Uri AUTHOR_GOOGLE_PLUS_URI = Uri.parse(
            "https://plus.google.com/105148560373589648355");

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.github)
    ViewGroup mGitHubLayout;
    @BindView(R.id.licenses)
    ViewGroup mLicensesLayout;
    @BindView(R.id.privacy_policy)
    ViewGroup mPrivacyPolicyLayout;
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
        mPrivacyPolicyLayout.setOnClickListener(view -> AppUtils.startActivity(IntentUtils.makeView(
                PRIVACY_POLICY_URI), this));
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
