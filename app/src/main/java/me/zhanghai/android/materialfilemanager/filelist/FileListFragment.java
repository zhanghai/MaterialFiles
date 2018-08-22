/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leinardi.android.speeddial.SpeedDialView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.FileJobService;
import me.zhanghai.android.materialfilemanager.file.FileProvider;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.filesystem.Files;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.IntentUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FileListFragment extends Fragment implements FileListAdapter.Listener,
        ConfirmDeleteFilesDialogFragment.Listener, RenameFileDialogFragment.Listener,
        CreateFileDialogFragment.Listener, CreateDirectoryDialogFragment.Listener {

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.breadcrumb)
    BreadcrumbLayout mBreadcrumbLayout;
    @BindView(R.id.content)
    ViewGroup mContentLayout;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.empty)
    View mEmptyView;
    @BindView(R.id.error)
    TextView mErrorView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.speed_dial)
    SpeedDialView mSpeedDialView;

    private MenuItem mSortByNameMenuItem;
    private MenuItem mSortByTypeMenuItem;
    private MenuItem mSortBySizeMenuItem;
    private MenuItem mSortByLastModifiedMenuItem;
    private MenuItem mSortOrderAscendingMenuItem;
    private MenuItem mSortDirectoriesFirstMenuItem;

    private FileListAdapter mAdapter;

    private FileViewModel mViewModel;

    private Uri mLastUri;

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

        int contentLayoutInitialPaddingBottom = mContentLayout.getPaddingBottom();
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) ->
                ViewUtils.setPaddingBottom(mContentLayout, contentLayoutInitialPaddingBottom
                        + mAppBarLayout.getTotalScrollRange() + verticalOffset));
        mBreadcrumbLayout.setOnItemSelectedListener(this::onBreadcrumbItemSelected);
        mSwipeRefreshLayout.setOnRefreshListener(this::reloadFile);
        mRecyclerView.setLayoutManager(new GridLayoutManager(activity, /*TODO*/ 1));
        mAdapter = new FileListAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mSpeedDialView.inflate(R.menu.file_list_speed_dial);
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.action_create_file:
                    onCreateFile();
                    break;
                case R.id.action_create_directory:
                    onCreateDirectory();
                    break;
            }
            // Returning false causes the speed dial to close without animation.
            //return false;
            mSpeedDialView.close();
            return true;
        });

        mViewModel = ViewModelProviders.of(this).get(FileViewModel.class);
        mViewModel.getSortOptionsData().observe(this, this::onSortOptionsChanged);
        mViewModel.getFileListData().observe(this, this::onFileListChanged);

        // TODO: Request storage permission.
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.file_list, menu);
        mSortByNameMenuItem = menu.findItem(R.id.action_sort_by_name);
        mSortByTypeMenuItem = menu.findItem(R.id.action_sort_by_type);
        mSortBySizeMenuItem = menu.findItem(R.id.action_sort_by_size);
        mSortByLastModifiedMenuItem = menu.findItem(R.id.action_sort_by_last_modified);
        mSortOrderAscendingMenuItem = menu.findItem(R.id.action_sort_order_ascending);
        mSortDirectoriesFirstMenuItem = menu.findItem(R.id.action_sort_directories_first);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        updateSortOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // TODO
                return true;
            case R.id.action_sort_by_name:
                setSortBy(FileSortOptions.By.NAME);
                return true;
            case R.id.action_sort_by_type:
                setSortBy(FileSortOptions.By.TYPE);
                return true;
            case R.id.action_sort_by_size:
                setSortBy(FileSortOptions.By.SIZE);
                return true;
            case R.id.action_sort_by_last_modified:
                setSortBy(FileSortOptions.By.LAST_MODIFIED);
                return true;
            case R.id.action_sort_order_ascending:
                setSortOrder(!mSortOrderAscendingMenuItem.isChecked() ?
                        FileSortOptions.Order.ASCENDING : FileSortOptions.Order.DESCENDING);
                return true;
            case R.id.action_sort_directories_first:
                setSortDirectoriesFirst(!mSortDirectoriesFirstMenuItem.isChecked());
                return true;
            case R.id.action_refresh:
                reloadFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed() {
        if (mSpeedDialView.isOpen()) {
            mSpeedDialView.close();
            return true;
        }
        return mViewModel.popPath();
    }

    private void onFileListChanged(FileListData fileListData) {
        switch (fileListData.state) {
            case LOADING: {
                Uri uri = fileListData.file.getUri();
                boolean isReload = Objects.equals(uri, mLastUri);
                mLastUri = uri;
                if (!isReload) {
                    mToolbar.setSubtitle(R.string.file_list_subtitle_loading);
                    updateBreadcrumbLayout();
                    ViewUtils.fadeIn(mProgress);
                    ViewUtils.fadeOut(mErrorView);
                    ViewUtils.fadeOut(mEmptyView);
                    mAdapter.clear();
                }
                break;
            }
            case ERROR:
                mToolbar.setSubtitle(R.string.file_list_subtitle_error);
                updateBreadcrumbLayout();
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorView);
                if (fileListData.exception instanceof FileSystemException) {
                    FileSystemException exception = (FileSystemException) fileListData.exception;
                    mErrorView.setText(exception.getMessage(mErrorView.getContext()));
                } else {
                    mErrorView.setText(fileListData.exception.toString());
                }
                ViewUtils.fadeOut(mEmptyView);
                break;
            case SUCCESS: {
                List<File> fileList = fileListData.fileList;
                updateSubtitle(fileList);
                updateBreadcrumbLayout();
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeToVisibility(mEmptyView, fileList.isEmpty());
                mAdapter.replaceAll(fileList);
                Parcelable state = mViewModel.getPendingState();
                if (state != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
                }
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private void updateSubtitle(List<File> files) {
        int directoryCount = Functional.reduce(files, (count, file) -> file.isDirectory() ?
                count + 1 : count, 0);
        int fileCount = files.size() - directoryCount;
        Resources resources = requireContext().getResources();
        String directoryCountText = directoryCount > 0 ? resources.getQuantityString(
                R.plurals.file_list_subtitle_directory_count_format, directoryCount, directoryCount)
                : null;
        String fileCountText = fileCount > 0 ? resources.getQuantityString(
                R.plurals.file_list_subtitle_file_count_format, fileCount, fileCount) : null;
        String subtitle;
        if (!TextUtils.isEmpty(directoryCountText) && !TextUtils.isEmpty(fileCountText)) {
            subtitle = directoryCountText + getString(R.string.file_list_subtitle_separator) +
                    fileCountText;
        } else if (!TextUtils.isEmpty(directoryCountText)) {
            subtitle = directoryCountText;
        } else if (!TextUtils.isEmpty(fileCountText)) {
            subtitle = fileCountText;
        } else {
            subtitle = getString(R.string.file_list_subtitle_empty);
        }
        mToolbar.setSubtitle(subtitle);
    }

    private void updateBreadcrumbLayout() {
        List<File> trail = mViewModel.getTrail();
        mBreadcrumbLayout.setItems(Functional.map(trail, File::getName),
                mViewModel.getTrailIndex());
    }

    private void setSortBy(FileSortOptions.By by) {
        FileSortOptions sortOptions = mViewModel.getSortOptions();
        if (sortOptions.getBy() == by) {
            return;
        }
        mViewModel.setSortOptions(sortOptions.withBy(by));
    }

    private void setSortOrder(FileSortOptions.Order order) {
        FileSortOptions sortOptions = mViewModel.getSortOptions();
        if (sortOptions.getOrder() == order) {
            return;
        }
        mViewModel.setSortOptions(sortOptions.withOrder(order));
    }

    private void setSortDirectoriesFirst(boolean directoriesFirst) {
        FileSortOptions sortOptions = mViewModel.getSortOptions();
        if (sortOptions.isDirectoriesFirst() == directoriesFirst) {
            return;
        }
        mViewModel.setSortOptions(sortOptions.withDirectoriesFirst(directoriesFirst));
    }

    private void onSortOptionsChanged(FileSortOptions sortOptions) {
        mAdapter.setComparator(sortOptions.makeComparator());
        updateSortOptionsMenu();
    }

    private void updateSortOptionsMenu() {
        if (mSortByNameMenuItem == null || mSortByTypeMenuItem == null
                || mSortBySizeMenuItem == null || mSortByLastModifiedMenuItem == null
                || mSortOrderAscendingMenuItem == null || mSortDirectoriesFirstMenuItem == null) {
            return;
        }
        MenuItem checkedSortByMenuItem;
        FileSortOptions sortOptions = mViewModel.getSortOptions();
        switch (sortOptions.getBy()) {
            case NAME:
                checkedSortByMenuItem = mSortByNameMenuItem;
                break;
            case TYPE:
                checkedSortByMenuItem = mSortByTypeMenuItem;
                break;
            case SIZE:
                checkedSortByMenuItem = mSortBySizeMenuItem;
                break;
            case LAST_MODIFIED:
                checkedSortByMenuItem = mSortByLastModifiedMenuItem;
                break;
            default:
                throw new IllegalStateException();
        }
        checkedSortByMenuItem.setChecked(true);
        mSortOrderAscendingMenuItem.setChecked(sortOptions.getOrder()
                == FileSortOptions.Order.ASCENDING);
        mSortDirectoriesFirstMenuItem.setChecked(sortOptions.isDirectoriesFirst());
    }

    private void reloadFile() {
        mSwipeRefreshLayout.setRefreshing(true);
        mViewModel.reload();
    }

    private void onBreadcrumbItemSelected(int index) {
        navigateToFile(mViewModel.getTrail().get(index));
    }

    @Override
    public void onOpenFile(File file) {
        if (file.isListable()) {
            navigateToFile(file.asListableFile());
            return;
        }
        if (file instanceof LocalFile) {
            LocalFile localFile = (LocalFile) file;
            java.io.File javaFile = localFile.makeJavaFile();
            Intent intent = IntentUtils.makeView(FileProvider.getUriForFile(javaFile),
                    file.getMimeType())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            AppUtils.startActivity(intent, requireContext());
        } else {
            // TODO
        }
    }

    private void navigateToFile(File file) {
        Parcelable state = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mViewModel.pushPath(state, file.makeBreadcrumbPath());
    }

    @Override
    public void onOpenFileAs(File file) {

    }

    @Override
    public void onCutFile(File file) {

    }

    @Override
    public void onCopyFile(File file) {

    }

    @Override
    public void onDeleteFile(File file) {
        ConfirmDeleteFilesDialogFragment.show(file, this);
    }

    @Override
    public void deleteFiles(List<File> files) {
        // TODO
        FileJobService.delete(files, requireContext());
    }

    @Override
    public void onRenameFile(File file) {
        RenameFileDialogFragment.show(file, this);
    }

    @Override
    public boolean hasFileWithName(String name) {
        FileListData fileListData = mViewModel.getFileListData().getValue();
        if (fileListData == null || fileListData.state != FileListData.State.SUCCESS) {
            return false;
        }
        return Functional.some(fileListData.fileList, file -> TextUtils.equals(file.getName(),
                name));
    }

    @Override
    public void renameFile(File file, String name) {
        // TODO
        FileJobService.rename(file, name, requireContext());
    }

    @Override
    public void onOpenFileProperties(File file) {

    }

    private void onCreateFile() {
        CreateFileDialogFragment.show(this);
    }

    @Override
    public void createFile(String name) {
        // TODO
        File file = Files.childOf(mViewModel.getPathFile(), name);
        FileJobService.createFile(file, requireContext());
    }

    private void onCreateDirectory() {
        CreateDirectoryDialogFragment.show(this);
    }

    @Override
    public void createDirectory(String name) {
        // TODO
        File file = Files.childOf(mViewModel.getPathFile(), name);
        FileJobService.createDirectory(file, requireContext());
    }
}
