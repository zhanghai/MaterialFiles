/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filesystem.Documents;
import me.zhanghai.android.files.util.AppUtils;

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

    @NonNull
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
        mFileListListener.observeCurrentPath(this, this::onCurrentPathChanged);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_DOCUMENT_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        addDocumentTree(uri);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onNavigationItemsChanged(@NonNull List<NavigationItem> navigationItems) {
        mAdapter.replace(navigationItems);
    }

    private void onCurrentPathChanged(@NonNull Path path) {
        mAdapter.notifyCheckedChanged();
    }

    @NonNull
    @Override
    public Path getCurrentPath() {
        return mFileListListener.getCurrentPath();
    }

    @Override
    public void navigateTo(@NonNull Path path) {
        mFileListListener.navigateTo(path);
    }

    @Override
    public void navigateToRoot(@NonNull Path path) {
        mFileListListener.navigateToRoot(path);
    }

    @Override
    public void onAddDocumentTree() {
        AppUtils.startActivityForResult(Documents.makeOpenTreeIntent(),
                REQUEST_CODE_OPEN_DOCUMENT_TREE, this);
    }

    private void addDocumentTree(@NonNull Uri uri) {
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
        Path getCurrentPath();

        void navigateTo(@NonNull Path path);

        void navigateToRoot(@NonNull Path path);

        void observeCurrentPath(@NonNull LifecycleOwner owner, @NonNull Observer<Path> observer);
    }
}
