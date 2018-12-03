/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

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

public class StandardDirectoriesActivityFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @NonNull
    public static StandardDirectoriesActivityFragment newInstance() {
        //noinspection deprecation
        return new StandardDirectoriesActivityFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public StandardDirectoriesActivityFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.standard_directories_activity_fragment, container, false);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //AppCompatActivity activity = (AppCompatActivity) requireActivity();
                //activity.onSupportNavigateUp();
                requireActivity().finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
