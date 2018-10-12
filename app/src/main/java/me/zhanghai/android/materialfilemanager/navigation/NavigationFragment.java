/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;

public class NavigationFragment extends Fragment implements NavigationItem.Listener {

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @NonNull
    private NavigationAdapter mAdapter;

    @NonNull
    private Listener mListener;

    public static NavigationFragment newInstance() {
        //noinspection deprecation
        return new NavigationFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public NavigationFragment() {}

    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        // TODO: Needed?
        //mRecyclerView.setItemAnimator(new NoChangeAnimationItemAnimator());
        Context context = requireContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new NavigationAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        NavigationItemListLiveData.getInstance().observe(this, this::onNavigationItemsChanged);
    }

    private void onNavigationItemsChanged(List<NavigationItem> navigationItems) {
        mAdapter.replace(navigationItems);
    }

    @NonNull
    @Override
    public File getCurrentFile() {
        return mListener.getCurrentFile();
    }

    @Override
    public void navigateToFile(@NonNull File file) {
        mListener.navigateToFile(file);
    }

    public interface Listener {

        @NonNull
        File getCurrentFile();

        void navigateToFile(@NonNull File file);
    }
}
