/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import me.zhanghai.android.materialfilemanager.filesystem.Documents;
import me.zhanghai.android.materialfilemanager.filesystem.File;

public class NavigationFragment extends Fragment implements NavigationItem.Listener {

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 1;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @NonNull
    private NavigationListAdapter mAdapter;

    @NonNull
    private MainListener mMainListener;
    @NonNull
    private FileListListener mFileListListener;

    public static NavigationFragment newInstance() {
        //noinspection deprecation
        return new NavigationFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public NavigationFragment() {}

    public void setListeners(@NonNull MainListener mainListener,
                             @NonNull FileListListener fileListListener) {
        mMainListener = mainListener;
        mFileListListener = fileListListener;
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
        mAdapter = new NavigationListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        NavigationItemListLiveData.getInstance().observe(this, this::onNavigationItemsChanged);
        mFileListListener.observeCurrentFile(this, this::onCurrentFileChanged);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_DOCUMENT_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    addDocumentTree(data.getData());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onNavigationItemsChanged(List<NavigationItem> navigationItems) {
        mAdapter.replace(navigationItems);
    }

    private void onCurrentFileChanged(File file) {
        mAdapter.notifyCheckedChanged();
    }

    @NonNull
    @Override
    public File getCurrentFile() {
        return mFileListListener.getCurrentFile();
    }

    @Override
    public void navigateToFile(@NonNull File file) {
        mFileListListener.navigateToFile(file);
    }

    @Override
    public void navigateToRoot(@NonNull File file) {
        mFileListListener.navigateToRoot(file);
    }

    @Override
    public void onAddDocumentTree() {
        startActivityForResult(Documents.makeOpenTreeIntent(), REQUEST_CODE_OPEN_DOCUMENT_TREE);
    }

    private void addDocumentTree(Uri uri) {
        // TODO: Support DocumentsProvider and add to navigation roots.
        //Documents.takePersistableTreePermission(uri, requireContext());
    }

    @Override
    public void closeNavigationDrawer() {
        mMainListener.closeNavigationDrawer();
    }

    public interface MainListener {

        void closeNavigationDrawer();
    }

    public interface FileListListener {

        @NonNull
        File getCurrentFile();

        void navigateToFile(@NonNull File file);

        void navigateToRoot(@NonNull File file);

        void observeCurrentFile(@NonNull LifecycleOwner owner, @NonNull Observer<File> observer);
    }
}
