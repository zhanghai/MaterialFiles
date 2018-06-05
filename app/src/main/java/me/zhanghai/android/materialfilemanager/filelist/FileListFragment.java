/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class FileListFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.breadcrumb)
    BreadcrumbLayout mBreadcrumbLayout;
    @BindView(R.id.files)
    RecyclerView mFileList;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private FileListAdapter mAdapter;

    private FileViewModel mViewModel;

    public static FileListFragment newInstance() {
        //noinspection deprecation
        return new FileListFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public FileListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_list_fragment, container, false);
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
        // TODO
        mBreadcrumbLayout.setItems(Arrays.asList("root/storage/emulated/0/Music".split("/")));
        mBreadcrumbLayout.setOnItemSelectedListener(this::onBreadcrumbItemSelected);
        mFileList.setLayoutManager(new GridLayoutManager(activity, /*TODO*/ 1));
        mAdapter = new FileListAdapter(this::onFileSelected);
        mFileList.setAdapter(mAdapter);

        mViewModel = ViewModelProviders.of(this).get(FileViewModel.class);
        mViewModel.getFileData().observe(this, this::onFileChanged);

        // TODO: Request storage permission.
        // TODO
        mViewModel.setPath(Uri.parse("file:///storage/emulated/0/Music"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.directory, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onBreadcrumbItemSelected(int index) {
        onListableFileSelected(mViewModel.getPathHistory().getFileAt(index));
    }

    private void onFileSelected(File file) {
        if (file.isListable()) {
            onListableFileSelected(file);
        } else {
            // TODO
        }
    }

    private void onListableFileSelected(File file) {
        List<PathHistory.Segment> segments = file.makePathSegments();
        mViewModel.getPathHistory().push(segments);
        mViewModel.setPath(file.getPath());
        mBreadcrumbLayout.setItems(Functional.map(segments, segment -> segment.title));
    }

    private void onFileChanged(File file) {
        List<File> files = file.getFileList();
        int directoryCount = Functional.reduce(files, (count, file_) -> file_.isDirectory() ?
                count + 1 : count, 0);
        int fileCount = files.size() - directoryCount;
        Resources resources = requireContext().getResources();
        String directoryCountText = directoryCount > 0 ? resources.getQuantityString(
                R.plurals.main_subtitle_directory_count_format, directoryCount, directoryCount)
                : null;
        String fileCountText = fileCount > 0 ? resources.getQuantityString(
                R.plurals.main_subtitle_file_count_format, fileCount, fileCount) : null;
        String subtitle;
        if (!TextUtils.isEmpty(directoryCountText) && !TextUtils.isEmpty(fileCountText)) {
            subtitle = directoryCountText + getString(R.string.main_subtitle_separator) +
                    fileCountText;
        } else if (!TextUtils.isEmpty(directoryCountText)) {
            subtitle = directoryCountText;
        } else if (!TextUtils.isEmpty(fileCountText)) {
            subtitle = fileCountText;
        } else {
            subtitle = getString(R.string.main_subtitle_empty);
        }
        mToolbar.setSubtitle(subtitle);
        mAdapter.submitList(files);
    }
}
