/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.effortlesspermissions.AfterPermissionDenied;
import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.effortlesspermissions.OpenAppDetailsDialogFragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.fileproperties.FilePropertiesDialogFragment;
import me.zhanghai.android.files.navigation.BookmarkDirectories;
import me.zhanghai.android.files.navigation.BookmarkDirectory;
import me.zhanghai.android.files.navigation.NavigationFragment;
import me.zhanghai.android.files.navigation.NavigationRoot;
import me.zhanghai.android.files.navigation.NavigationRootMapLiveData;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.terminal.Terminal;
import me.zhanghai.android.files.ui.FixQueryChangeSearchView;
import me.zhanghai.android.files.ui.OverlayToolbarActionMode;
import me.zhanghai.android.files.ui.PersistentBarLayout;
import me.zhanghai.android.files.ui.PersistentBarLayoutToolbarActionMode;
import me.zhanghai.android.files.ui.PersistentDrawerLayout;
import me.zhanghai.android.files.ui.ToolbarActionMode;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.ClipboardUtils;
import me.zhanghai.android.files.util.CollectionUtils;
import me.zhanghai.android.files.util.DebouncedRunnable;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;
import me.zhanghai.android.files.util.ToastUtils;
import me.zhanghai.android.files.util.ViewUtils;
import me.zhanghai.android.files.viewer.image.ImageViewerActivity;
import me.zhanghai.java.functional.Functional;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class FileListFragment extends Fragment implements BreadcrumbLayout.Listener,
        FileListAdapter.Listener, OpenApkDialogFragment.Listener,
        ConfirmDeleteFilesDialogFragment.Listener, CreateArchiveDialogFragment.Listener,
        RenameFileDialogFragment.Listener, CreateFileDialogFragment.Listener,
        CreateDirectoryDialogFragment.Listener, NavigationFragment.Listener {

    private static final int REQUEST_CODE_STORAGE_PERMISSIONS = 1;

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Intent mIntent;
    @Nullable
    private Path mExtraPath;

    private NavigationFragment mNavigationFragment;

    @BindView(R.id.drawer)
    @Nullable
    DrawerLayout mDrawerLayout;
    @BindView(R.id.persistent_drawer)
    @Nullable
    PersistentDrawerLayout mPersistentDrawerLayout;
    @BindView(R.id.bar_layout)
    PersistentBarLayout mPersistentBarLayout;
    @BindView(R.id.bottom_bar)
    ViewGroup mBottomBarLayout;
    @BindView(R.id.bottom_toolbar)
    Toolbar mBottomToolbar;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.overlay_toolbar)
    Toolbar mOverlayToolbar;
    @BindView(R.id.breadcrumb)
    BreadcrumbLayout mBreadcrumbLayout;
    @BindView(R.id.content)
    ViewGroup mContentLayout;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorText;
    @BindView(R.id.empty)
    View mEmptyView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.speed_dial)
    SpeedDialView mSpeedDialView;

    @Nullable
    private MenuItem mSearchMenuItem;
    @Nullable
    private MenuItem mSortMenuItem;
    @Nullable
    private MenuItem mSortByNameMenuItem;
    @Nullable
    private MenuItem mSortByTypeMenuItem;
    @Nullable
    private MenuItem mSortBySizeMenuItem;
    @Nullable
    private MenuItem mSortByLastModifiedMenuItem;
    @Nullable
    private MenuItem mSortOrderAscendingMenuItem;
    @Nullable
    private MenuItem mSortDirectoriesFirstMenuItem;
    @Nullable
    private MenuItem mSortPathSpecificMenuItem;
    @Nullable
    private MenuItem mSelectAllMenuItem;
    @Nullable
    private MenuItem mShowHiddenFilesMenuItem;

    @NonNull
    private ToolbarActionMode mOverlayActionMode;
    @NonNull
    private ToolbarActionMode mBottomActionMode;

    @NonNull
    private FileListAdapter mAdapter;

    @NonNull
    private FileListViewModel mViewModel;

    @Nullable
    private Path mLastLoadingPath;
    private boolean mLastLoadingSearching;

    @NonNull
    private final DebouncedRunnable mDebouncedSearchRunnable = new DebouncedRunnable(() -> {
        if (!isResumed() || !mViewModel.isSearchViewExpanded()) {
            return;
        }
        String query = mViewModel.getSearchViewQuery();
        if (query.isEmpty()) {
            return;
        }
        mViewModel.search(query);
    }, 1000, new Handler(Looper.getMainLooper()));

    @NonNull
    public static FileListFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        FileListFragment fragment = new FileListFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(Intent.EXTRA_INTENT, intent);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public FileListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = BundleUtils.getParcelable(getArguments(), Intent.EXTRA_INTENT);
        mExtraPath = IntentPathUtils.getExtraPath(mIntent);

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

        if (savedInstanceState == null) {
            mNavigationFragment = NavigationFragment.newInstance();
            FragmentUtils.add(mNavigationFragment, this, R.id.navigation_fragment);
        } else {
            mNavigationFragment = FragmentUtils.findById(this, R.id.navigation_fragment);
        }
        mNavigationFragment.setListeners(this);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mOverlayActionMode = new OverlayToolbarActionMode(mOverlayToolbar);
        mBottomActionMode = new PersistentBarLayoutToolbarActionMode(mPersistentBarLayout,
                mBottomBarLayout, mBottomToolbar);

        int contentLayoutInitialPaddingBottom = mContentLayout.getPaddingBottom();
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) ->
                ViewUtils.setPaddingBottom(mContentLayout, contentLayoutInitialPaddingBottom
                        + mAppBarLayout.getTotalScrollRange() + verticalOffset));
        mBreadcrumbLayout.setListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        mRecyclerView.setLayoutManager(new GridLayoutManager(activity, /*TODO*/ 1));
        mAdapter = new FileListAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mSpeedDialView.inflate(R.menu.file_list_speed_dial);
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.action_create_file:
                    showCreateFileDialog();
                    break;
                case R.id.action_create_directory:
                    showCreateDirectoryDialog();
                    break;
            }
            // Returning false causes the speed dial to close without animation.
            //return false;
            mSpeedDialView.close();
            return true;
        });

        mViewModel = ViewModelProviders.of(this).get(FileListViewModel.class);
        if (!mViewModel.hasTrail()) {
            Path path = mExtraPath;
            PickOptions pickOptions = null;
            String action = mIntent.getAction();
            if (action == null) {
                action = Intent.ACTION_VIEW;
            }
            switch (action) {
                case Intent.ACTION_GET_CONTENT:
                case Intent.ACTION_OPEN_DOCUMENT:
                case Intent.ACTION_CREATE_DOCUMENT: {
                    boolean readOnly = Objects.equals(action, Intent.ACTION_GET_CONTENT);
                    List<String> mimeTypes = Collections.singletonList(mIntent.getType());
                    String[] extraMimeTypes = mIntent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES);
                    if (extraMimeTypes != null) {
                        mimeTypes = Arrays.asList(extraMimeTypes);
                    }
                    boolean localOnly = mIntent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false);
                    boolean allowMultiple = mIntent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE,
                            false);
                    // TODO: Actually support ACTION_CREATE_DOCUMENT.
                    pickOptions = new PickOptions(readOnly, false, mimeTypes, localOnly,
                            allowMultiple);
                    break;
                }
                case Intent.ACTION_OPEN_DOCUMENT_TREE:
                    boolean localOnly = mIntent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false);
                    pickOptions = new PickOptions(false, true, Collections.emptyList(), localOnly,
                            false);
                    break;
                case Intent.ACTION_VIEW:
                default:
                    if (path != null) {
                        String mimeType = mIntent.getType();
                        if (mimeType != null && FileUtils.isArchiveFile(path, mimeType)) {
                            path = ArchiveFileSystemProvider.getRootPathForArchiveFile(path);
                        }
                    }
            }
            if (path == null) {
                path = Settings.FILE_LIST_DEFAULT_DIRECTORY.getValue();
            }
            mViewModel.resetTo(path);
            if (pickOptions != null) {
                mViewModel.setPickOptions(pickOptions);
            }
        }
        if (mPersistentDrawerLayout != null) {
            Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.observe(this,
                    this::onPersistentDrawerOpenChanged);
        }
        mViewModel.getCurrentPathLiveData().observe(this, this::onCurrentPathChanged);
        mViewModel.getSearchViewExpandedLiveData().observe(this, this::onSearchViewExpandedChanged);
        mViewModel.getBreadcrumbLiveData().observe(this, mBreadcrumbLayout::setData);
        mViewModel.getSortOptionsLiveData().observe(this, this::onSortOptionsChanged);
        mViewModel.getSortPathSpecificLiveData().observe(this, this::onSortPathSpecificChanged);
        mViewModel.getPickOptionsLiveData().observe(this, this::onPickOptionsChanged);
        mViewModel.getSelectedFilesLiveData().observe(this, this::onSelectedFilesChanged);
        mViewModel.getPasteStateLiveData().observe(this, this::onPasteStateChanged);
        mViewModel.getFileListLiveData().observe(this, this::onFileListChanged);
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.observe(this, this::onShowHiddenFilesChanged);

        if (!EffortlessPermissions.hasPermissions(this, STORAGE_PERMISSIONS)) {
            EffortlessPermissions.requestPermissions(this,
                    R.string.storage_permission_request_message, REQUEST_CODE_STORAGE_PERMISSIONS,
                    STORAGE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EffortlessPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                this);
    }

    @AfterPermissionGranted(REQUEST_CODE_STORAGE_PERMISSIONS)
    private void onStoragePermissionGranted() {
        mViewModel.reload();
    }

    @AfterPermissionDenied(REQUEST_CODE_STORAGE_PERMISSIONS)
    private void onStoragePermissionDenied() {
        if (EffortlessPermissions.somePermissionPermanentlyDenied(this,
                STORAGE_PERMISSIONS)) {
            OpenAppDetailsDialogFragment.show(
                    R.string.storage_permission_permanently_denied_message,
                    R.string.open_settings, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.file_list, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSortMenuItem = menu.findItem(R.id.action_sort);
        mSortByNameMenuItem = menu.findItem(R.id.action_sort_by_name);
        mSortByTypeMenuItem = menu.findItem(R.id.action_sort_by_type);
        mSortBySizeMenuItem = menu.findItem(R.id.action_sort_by_size);
        mSortByLastModifiedMenuItem = menu.findItem(R.id.action_sort_by_last_modified);
        mSortOrderAscendingMenuItem = menu.findItem(R.id.action_sort_order_ascending);
        mSortDirectoriesFirstMenuItem = menu.findItem(R.id.action_sort_directories_first);
        mSortPathSpecificMenuItem = menu.findItem(R.id.action_sort_path_specific);
        mSelectAllMenuItem = menu.findItem(R.id.action_select_all);
        mShowHiddenFilesMenuItem = menu.findItem(R.id.action_show_hidden_files);

        setUpSearchView();
    }

    private void setUpSearchView() {
        FixQueryChangeSearchView searchView = (FixQueryChangeSearchView)
                mSearchMenuItem.getActionView();
        // MenuItem.OnActionExpandListener.onMenuItemActionExpand() is called before SearchView
        // resets the query.
        searchView.setOnSearchClickListener(view -> {
            mViewModel.setSearchViewExpanded(true);
            searchView.setQuery(mViewModel.getSearchViewQuery(), false);
            mDebouncedSearchRunnable.run();
        });
        // SearchView.OnCloseListener.onClose() is not always called.
        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                mViewModel.setSearchViewExpanded(false);
                mViewModel.stopSearching();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                mDebouncedSearchRunnable.cancel();
                mViewModel.search(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(@NonNull String query) {
                if (searchView.shouldIgnoreQueryChange()) {
                    return false;
                }
                mViewModel.setSearchViewQuery(query);
                mDebouncedSearchRunnable.run();
                return false;
            }
        });
        if (mViewModel.isSearchViewExpanded()) {
            mSearchMenuItem.expandActionView();
        }
    }

    private void collapseSearchView() {
        if (mSearchMenuItem != null && mSearchMenuItem.isActionViewExpanded()) {
            mSearchMenuItem.collapseActionView();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        updateSortMenuItems();
        updateSelectAllMenuItem();
        updateShowHiddenFilesMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout != null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                if (mPersistentDrawerLayout != null) {
                    Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.putValue(
                            !Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.getValue());
                }
                return true;
            case R.id.action_sort_by_name:
                mViewModel.setSortBy(FileSortOptions.By.NAME);
                return true;
            case R.id.action_sort_by_type:
                mViewModel.setSortBy(FileSortOptions.By.TYPE);
                return true;
            case R.id.action_sort_by_size:
                mViewModel.setSortBy(FileSortOptions.By.SIZE);
                return true;
            case R.id.action_sort_by_last_modified:
                mViewModel.setSortBy(FileSortOptions.By.LAST_MODIFIED);
                return true;
            case R.id.action_sort_order_ascending:
                mViewModel.setSortOrder(!mSortOrderAscendingMenuItem.isChecked() ?
                        FileSortOptions.Order.ASCENDING : FileSortOptions.Order.DESCENDING);
                return true;
            case R.id.action_sort_directories_first:
                mViewModel.setSortDirectoriesFirst(!mSortDirectoriesFirstMenuItem.isChecked());
                return true;
            case R.id.action_sort_path_specific:
                mViewModel.setSortPathSpecific(!mSortPathSpecificMenuItem.isChecked());
                return true;
            case R.id.action_new_task:
                newTask();
                return true;
            case R.id.action_navigate_up:
                navigateUp();
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_select_all:
                selectAllFiles();
                return true;
            case R.id.action_show_hidden_files:
                setShowHiddenFiles(!mShowHiddenFilesMenuItem.isChecked());
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_copy_path:
                copyPath();
                return true;
            case R.id.action_open_in_terminal:
                openInTerminal();
                return true;
            case R.id.action_add_bookmark:
                addBookmark();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (mSpeedDialView.isOpen()) {
            mSpeedDialView.close();
            return true;
        }
        if (mOverlayActionMode.isActive()) {
            mOverlayActionMode.finish();
            return true;
        }
        return mViewModel.navigateUp(false);
    }

    private void onPersistentDrawerOpenChanged(boolean open) {
        if (mPersistentDrawerLayout != null) {
            if (open) {
                mPersistentDrawerLayout.openDrawer(GravityCompat.START);
            } else {
                mPersistentDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }
    }

    private void onCurrentPathChanged(@NonNull Path path) {
        updateOverlayToolbar();
        updateBottomToolbar();
    }

    private void onSearchViewExpandedChanged(boolean expanded) {
        updateSortMenuItems();
    }

    private void onFileListChanged(@NonNull FileListData fileListData) {
        switch (fileListData.state) {
            case LOADING: {
                Path path = mViewModel.getCurrentPath();
                boolean searching = mViewModel.getSearchState().searching;
                boolean isReload = Objects.equals(path, mLastLoadingPath)
                        && searching == mLastLoadingSearching;
                mLastLoadingPath = path;
                mLastLoadingSearching = searching;
                if (searching) {
                    updateSubtitle(fileListData.data);
                    mSwipeRefreshLayout.setRefreshing(true);
                    ViewUtils.fadeOut(mProgress);
                    ViewUtils.fadeOut(mErrorText);
                    // We are still searching so it's never empty.
                    ViewUtils.fadeOut(mEmptyView);
                    updateAdapterFileList();
                } else if (isReload) {
                    mSwipeRefreshLayout.setRefreshing(true);
                } else {
                    mToolbar.setSubtitle(R.string.loading);
                    mSwipeRefreshLayout.setRefreshing(false);
                    ViewUtils.fadeIn(mProgress);
                    ViewUtils.fadeOut(mErrorText);
                    ViewUtils.fadeOut(mEmptyView);
                    mAdapter.clear();
                }
                break;
            }
            case ERROR:
                fileListData.exception.printStackTrace();
                mToolbar.setSubtitle(R.string.error);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorText);
                mErrorText.setText(fileListData.exception.toString());
                ViewUtils.fadeOut(mEmptyView);
                mAdapter.clear();
                break;
            case SUCCESS: {
                updateSubtitle(fileListData.data);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorText);
                ViewUtils.fadeToVisibility(mEmptyView, fileListData.data.isEmpty());
                updateAdapterFileList();
                Parcelable state = mViewModel.getPendingState();
                if (state != null) {
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
                }
                break;
            }
            default:
                throw new AssertionError();
        }
    }

    private void updateSubtitle(@NonNull List<FileItem> files) {
        int directoryCount = Functional.reduce(files, (count, file) ->
                file.getAttributes().isDirectory() ? count + 1 : count, 0);
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
            subtitle = getString(R.string.empty);
        }
        mToolbar.setSubtitle(subtitle);
    }

    private void onSortOptionsChanged(@NonNull FileSortOptions sortOptions) {
        mAdapter.setComparator(sortOptions.makeComparator());
        updateSortMenuItems();
    }

    private void onSortPathSpecificChanged(boolean pathSpecific) {
        updateSortMenuItems();
    }

    private void updateSortMenuItems() {
        if (mSortMenuItem == null || mSortByNameMenuItem == null || mSortByTypeMenuItem == null
                || mSortBySizeMenuItem == null || mSortByLastModifiedMenuItem == null
                || mSortOrderAscendingMenuItem == null || mSortDirectoriesFirstMenuItem == null
                || mSortPathSpecificMenuItem == null) {
            return;
        }
        boolean searchViewExpanded = mViewModel.isSearchViewExpanded();
        mSortMenuItem.setVisible(!searchViewExpanded);
        if (searchViewExpanded) {
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
        mSortPathSpecificMenuItem.setChecked(mViewModel.isSortPathSpecific());
    }

    private void navigateUp() {
        collapseSearchView();
        mViewModel.navigateUp(true);
    }

    private void newTask() {
        openInNewTask(getCurrentPath());
    }

    private void refresh() {
        mViewModel.reload();
    }

    private void setShowHiddenFiles(boolean showHiddenFiles) {
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(showHiddenFiles);
    }

    private void onShowHiddenFilesChanged(boolean showHiddenFiles) {
        updateAdapterFileList();
        updateShowHiddenFilesMenuItem();
    }

    private void updateAdapterFileList() {
        FileListData fileListData = mViewModel.getFileListData();
        if (fileListData.data == null) {
            return;
        }
        List<FileItem> files = fileListData.data;
        if (!Settings.FILE_LIST_SHOW_HIDDEN_FILES.getValue()) {
            files = Functional.filter(files, file -> !file.isHidden());
        }
        mAdapter.replace2(files, mViewModel.getSearchState().searching);
    }

    private void updateShowHiddenFilesMenuItem() {
        if (mShowHiddenFilesMenuItem == null) {
            return;
        }
        boolean showHiddenFiles = Settings.FILE_LIST_SHOW_HIDDEN_FILES.getValue();
        mShowHiddenFilesMenuItem.setChecked(showHiddenFiles);
    }

    private void share() {
        shareFile(getCurrentPath(), MimeTypes.DIRECTORY_MIME_TYPE);
    }

    private void copyPath() {
        copyPath(getCurrentPath());
    }

    private void openInTerminal() {
        Path path = getCurrentPath();
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            Terminal.open(path.toFile().getPath(), requireContext());
        } else {
            // TODO
        }
    }

    private void addBookmark() {
        addBookmark(getCurrentPath());
    }

    @Override
    public void navigateTo(@NonNull Path path) {
        collapseSearchView();
        Parcelable state = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mViewModel.navigateTo(state, path);
    }

    @Override
    public void copyPath(@NonNull Path path) {
        ClipboardUtils.copyText(FileUtils.getPathString(path), requireContext());
    }

    @Override
    public void openInNewTask(@NonNull Path path) {
        Intent intent = FileListActivity.newViewIntent(path, requireContext())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    private void onPickOptionsChanged(@Nullable PickOptions pickOptions) {
        String title;
        if (pickOptions == null) {
            title = getString(R.string.file_list_title);
        } else {
            int titleRes = pickOptions.pickDirectory ? R.plurals.file_list_title_pick_directory
                    : R.plurals.file_list_title_pick_file;
            int count = pickOptions.allowMultiple ? Integer.MAX_VALUE : 1;
            title = getResources().getQuantityString(titleRes, count);
        }
        requireActivity().setTitle(title);
        updateSelectAllMenuItem();
        updateOverlayToolbar();
        updateBottomToolbar();
        mAdapter.setPickOptions(pickOptions);
    }

    private void updateSelectAllMenuItem() {
        if (mSelectAllMenuItem == null) {
            return;
        }
        PickOptions pickOptions = mViewModel.getPickOptions();
        mSelectAllMenuItem.setVisible(pickOptions == null || pickOptions.allowMultiple);
    }

    private void pickFiles(@NonNull LinkedHashSet<FileItem> files) {
        pickPaths(Functional.map(files, FileItem::getPath, new LinkedHashSet<>()));
    }

    private void pickPaths(@NonNull LinkedHashSet<Path> paths) {
        Intent intent = new Intent();
        PickOptions pickOptions = mViewModel.getPickOptions();
        if (paths.size() == 1) {
            Path path = CollectionUtils.first(paths);
            Uri data = FileProvider.getUriForPath(path);
            intent.setData(data);
            IntentPathUtils.putExtraPath(intent, path);
        } else {
            String[] mimeTypes = pickOptions.mimeTypes.toArray(new String[0]);
            List<ClipData.Item> items = Functional.map(paths, path -> new ClipData.Item(
                    FileProvider.getUriForPath(path)));
            ClipData clipData = new ClipData(null, mimeTypes, items.get(0));
            for (int i = 1; i < items.size(); ++i) {
                clipData.addItem(items.get(i));
            }
            intent.setClipData(clipData);
            IntentPathUtils.putExtraPathList(intent, paths);
        }
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        if (!pickOptions.readOnly) {
            flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
        }
        if (pickOptions.pickDirectory) {
            flags |= Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
        }
        intent.addFlags(flags);
        Activity activity = requireActivity();
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    private void onSelectedFilesChanged(@NonNull LinkedHashSet<FileItem> files) {
        updateOverlayToolbar();
        mAdapter.replaceSelectedFiles(files);
    }

    private void updateOverlayToolbar() {
        LinkedHashSet<FileItem> files = mViewModel.getSelectedFiles();
        if (files.isEmpty()) {
            if (mOverlayActionMode.isActive()) {
                mOverlayActionMode.finish();
            }
            return;
        }
        PickOptions pickOptions = mViewModel.getPickOptions();
        if (pickOptions != null) {
            mOverlayActionMode.setTitle(getString(R.string.file_list_select_title_format,
                    files.size()));
            mOverlayActionMode.setMenuResource(R.menu.file_list_pick);
            Menu menu = mOverlayActionMode.getMenu();
            menu.findItem(R.id.action_select_all).setVisible(pickOptions.allowMultiple);
        } else {
            mOverlayActionMode.setTitle(getString(R.string.file_list_select_title_format,
                    files.size()));
            mOverlayActionMode.setMenuResource(R.menu.file_list_select);
            Menu menu = mOverlayActionMode.getMenu();
            boolean hasReadOnly = Functional.some(files, file ->
                    file.getPath().getFileSystem().isReadOnly());
            menu.findItem(R.id.action_cut).setVisible(!hasReadOnly);
            boolean isExtract = Functional.every(files, file ->
                    ArchiveFileSystemProvider.isArchivePath(file.getPath()));
            menu.findItem(R.id.action_copy)
                    .setIcon(isExtract ? R.drawable.extract_icon_white_24dp
                            : R.drawable.copy_icon_control_normal_24dp)
                    .setTitle(isExtract ? R.string.file_list_select_action_extract : R.string.copy);
            menu.findItem(R.id.action_delete).setVisible(!hasReadOnly);
        }
        if (!mOverlayActionMode.isActive()) {
            mAppBarLayout.setExpanded(true);
            mOverlayActionMode.start(new ToolbarActionMode.Callback() {
                @Override
                public void onToolbarActionModeStarted(
                        @NonNull ToolbarActionMode toolbarActionMode) {}
                @Override
                public boolean onToolbarActionModeItemClicked(
                        @NonNull ToolbarActionMode toolbarActionMode, @NonNull MenuItem item) {
                    return onOverlayActionModeItemClicked(toolbarActionMode, item);
                }
                @Override
                public void onToolbarActionModeFinished(
                        @NonNull ToolbarActionMode toolbarActionMode) {
                    onOverlayActionModeFinished(toolbarActionMode);
                }
            });
        }
    }

    private boolean onOverlayActionModeItemClicked(@NonNull ToolbarActionMode toolbarActionMode,
                                                   @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick:
                pickFiles(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_cut:
                cutFiles(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_copy:
                copyFiles(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_delete:
                confirmDeleteFiles(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_archive:
                showCreateArchiveDialog(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_share:
                shareFiles(mViewModel.getSelectedFiles());
                return true;
            case R.id.action_select_all:
                selectAllFiles();
                return true;
            default:
                return false;
        }
    }

    private void onOverlayActionModeFinished(@NonNull ToolbarActionMode toolbarActionMode) {
        mViewModel.clearSelectedFiles();
    }

    private void cutFiles(@NonNull LinkedHashSet<FileItem> files) {
        mViewModel.addToPasteState(false, files);
        mViewModel.selectFiles(files, false);
    }

    private void copyFiles(@NonNull LinkedHashSet<FileItem> files) {
        mViewModel.addToPasteState(true, files);
        mViewModel.selectFiles(files, false);
    }

    private void confirmDeleteFiles(@NonNull LinkedHashSet<FileItem> files) {
        ConfirmDeleteFilesDialogFragment.show(files, this);
    }

    @Override
    public void deleteFiles(@NonNull LinkedHashSet<FileItem> files) {
        FileJobService.delete(makePathListForJob(files), requireContext());
        mViewModel.selectFiles(files, false);
    }

    private void showCreateArchiveDialog(@NonNull LinkedHashSet<FileItem> files) {
        CreateArchiveDialogFragment.show(files, this);
    }

    @Override
    public void archive(@NonNull LinkedHashSet<FileItem> files, @NonNull String name,
                        @NonNull String archiveType, @Nullable String compressorType) {
        Path archiveFile = mViewModel.getCurrentPath().resolve(name);
        FileJobService.archive(makePathListForJob(files), archiveFile, archiveType, compressorType,
                requireContext());
        mViewModel.selectFiles(files, false);
    }

    private void shareFiles(@NonNull LinkedHashSet<FileItem> files) {
        shareFiles(Functional.map(files, FileItem::getPath), Functional.map(files,
                FileItem::getMimeType));
        mViewModel.selectFiles(files, false);
    }

    private void selectAllFiles() {
        mAdapter.selectAllFiles();
    }

    private void onPasteStateChanged(@NonNull PasteState pasteState) {
        updateBottomToolbar();
    }

    private void updateBottomToolbar() {
        PickOptions pickOptions = mViewModel.getPickOptions();
        if (pickOptions != null) {
            if (!pickOptions.pickDirectory) {
                if (mBottomActionMode.isActive()) {
                    mBottomActionMode.finish();
                }
                return;
            }
            mBottomActionMode.setNavigationIcon(R.drawable.check_icon_control_normal_24dp);
            Path path = mViewModel.getCurrentPath();
            NavigationRoot navigationRoot = NavigationRootMapLiveData.getInstance().getValue().get(
                    path);
            String name = navigationRoot != null ? navigationRoot.getName(requireContext())
                    : FileUtils.getName(path);
            mBottomActionMode.setTitle(getString(R.string.file_list_select_current_directory_format,
                    name));
        } else {
            PasteState pasteState = mViewModel.getPasteState();
            LinkedHashSet<FileItem> files = pasteState.files;
            if (files.isEmpty()) {
                if (mBottomActionMode.isActive()) {
                    mBottomActionMode.finish();
                }
                return;
            }
            mBottomActionMode.setNavigationIcon(R.drawable.close_icon_control_normal_24dp);
            boolean isExtract = Functional.every(files, file ->
                    ArchiveFileSystemProvider.isArchivePath(file.getPath()));
            mBottomActionMode.setTitle(getString(pasteState.copy ? isExtract ?
                    R.string.file_list_paste_extract_title_format
                    : R.string.file_list_paste_copy_title_format
                    : R.string.file_list_paste_move_title_format, files.size()));
            mBottomActionMode.setMenuResource(R.menu.file_list_paste);
            boolean isReadOnly = mViewModel.getCurrentPath().getFileSystem().isReadOnly();
            mBottomActionMode.getMenu().findItem(R.id.action_paste)
                    .setTitle(isExtract ? R.string.file_list_paste_action_extract_here : R.string.paste)
                    .setEnabled(!isReadOnly);
        }
        if (!mBottomActionMode.isActive()) {
            mBottomActionMode.start(new ToolbarActionMode.Callback() {
                @Override
                public void onToolbarActionModeStarted(
                        @NonNull ToolbarActionMode toolbarActionMode) {}
                @Override
                public boolean onToolbarActionModeItemClicked(
                        @NonNull ToolbarActionMode toolbarActionMode, @NonNull MenuItem item) {
                    return onBottomActionModeItemClicked(toolbarActionMode, item);
                }
                @Override
                public void onToolbarActionModeFinished(
                        @NonNull ToolbarActionMode toolbarActionMode) {
                    onBottomActionModeFinished(toolbarActionMode);
                }
            });
        }
    }

    private boolean onBottomActionModeItemClicked(@NonNull ToolbarActionMode toolbarActionMode,
                                                  @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_paste:
                pasteFiles(getCurrentPath());
                return true;
            default:
                return false;
        }
    }

    private void onBottomActionModeFinished(@NonNull ToolbarActionMode toolbarActionMode) {
        PickOptions pickOptions = mViewModel.getPickOptions();
        if (pickOptions != null) {
            if (pickOptions.pickDirectory) {
                pickPaths(CollectionUtils.singletonLinkedSet(mViewModel.getCurrentPath()));
            }
        } else {
            mViewModel.clearPasteState();
        }
    }

    private void pasteFiles(@NonNull Path targetDirectory) {
        PasteState pasteState = mViewModel.getPasteState();
        if (mViewModel.getPasteState().copy) {
            FileJobService.copy(makePathListForJob(pasteState.files), targetDirectory,
                    requireContext());
        } else {
            FileJobService.move(makePathListForJob(pasteState.files), targetDirectory,
                    requireContext());
        }
        mViewModel.clearPasteState();
    }

    @NonNull
    private List<Path> makePathListForJob(@NonNull LinkedHashSet<FileItem> files) {
        List<Path> pathList = Functional.map(files, FileItem::getPath);
        Collections.sort(pathList);
        return pathList;
    }

    @Override
    public void clearSelectedFiles() {
        mViewModel.clearSelectedFiles();
    }

    @Override
    public void selectFile(@NonNull FileItem file, boolean selected) {
        mViewModel.selectFile(file, selected);
    }

    @Override
    public void selectFiles(@NonNull LinkedHashSet<FileItem> files, boolean selected) {
        mViewModel.selectFiles(files, selected);
    }

    @Override
    public void openFile(@NonNull FileItem file) {
        PickOptions pickOptions = mViewModel.getPickOptions();
        if (pickOptions != null) {
            if (file.getAttributes().isDirectory()) {
                navigateTo(file.getPath());
            } else if (!pickOptions.pickDirectory) {
                pickFiles(CollectionUtils.singletonLinkedSet(file));
            }
            return;
        }
        if (MimeTypes.isApk(file.getMimeType())) {
            openApk(file);
            return;
        }
        if (FileUtils.isListable(file)) {
            navigateTo(FileUtils.toListablePath(file));
            return;
        }
        openFileWithIntent(file, false);
    }

    private void openApk(@NonNull FileItem file) {
        if (!FileUtils.isListable(file)) {
            installApk(file);
            return;
        }
        switch (Settings.OPEN_APK_DEFAULT_ACTION.getValue()) {
            case INSTALL:
                installApk(file);
                break;
            case VIEW:
                viewApk(file);
                break;
            case ASK:
                OpenApkDialogFragment.show(file, this);
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void installApk(@NonNull FileItem file) {
        openFileWithIntent(file, false);
    }

    @Override
    public void viewApk(@NonNull FileItem file) {
        navigateTo(FileUtils.toListablePath(file));
    }

    @Override
    public void openFileWith(@NonNull FileItem file) {
        openFileWithIntent(file, true);
    }

    private void openFileWithIntent(@NonNull FileItem file, boolean withChooser) {
        Path path = file.getPath();
        String mimeType = file.getMimeType();
        Context context = requireContext();
        if (LinuxFileSystemProvider.isLinuxPath(path)
                || DocumentFileSystemProvider.isDocumentPath(path)) {
            Uri uri = FileProvider.getUriForPath(path);
            Intent intent = IntentUtils.makeView(uri, mimeType)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            IntentPathUtils.putExtraPath(intent, path);
            maybeAddImageViewerActivityExtras(intent, path, mimeType);
            if (withChooser) {
                intent = IntentUtils.withChooser(intent);
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        new Parcelable[] { OpenFileAsDialogActivity.newIntent(path, context) });
            }
            AppUtils.startActivity(intent, this);
        } else {
            FileJobService.open(path, mimeType, withChooser, context);
        }
    }

    private void maybeAddImageViewerActivityExtras(@NonNull Intent intent, @NonNull Path path,
                                                   @NonNull String mimeType) {
        if (!MimeTypes.isImage(mimeType)) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        // We need the ordered list from our adapter instead of the list from FileListLiveData.
        for (int i = 0; i < mAdapter.getItemCount(); ++i) {
            FileItem file = mAdapter.getItem(i);
            Path filePath = file.getPath();
            if (MimeTypes.isImage(file.getMimeType()) || Objects.equals(filePath, path)) {
                paths.add(filePath);
            }
        }
        int position = paths.indexOf(path);
        if (position == -1) {
            return;
        }
        ImageViewerActivity.putExtras(intent, paths, position);
    }

    @Override
    public void cutFile(@NonNull FileItem file) {
        cutFiles(CollectionUtils.singletonLinkedSet(file));
    }

    @Override
    public void copyFile(@NonNull FileItem file) {
        copyFiles(CollectionUtils.singletonLinkedSet(file));
    }

    @Override
    public void confirmDeleteFile(@NonNull FileItem file) {
        confirmDeleteFiles(CollectionUtils.singletonLinkedSet(file));
    }

    @Override
    public void showRenameFileDialog(@NonNull FileItem file) {
        RenameFileDialogFragment.show(file, this);
    }

    @Override
    public boolean hasFileWithName(@NonNull String name) {
        FileListData fileListData = mViewModel.getFileListData();
        if (fileListData.state != FileListData.State.SUCCESS) {
            return false;
        }
        return Functional.some(fileListData.data, path -> Objects.equals(FileUtils.getName(
                path), name));
    }

    @Override
    public void renameFile(@NonNull FileItem file, @NonNull String newName) {
        FileJobService.rename(file.getPath(), newName, requireContext());
    }

    @Override
    public void extractFile(@NonNull FileItem file) {
        file = FileUtils.createDummyFileItemForArchiveRoot(file);
        copyFiles(CollectionUtils.singletonLinkedSet(file));
    }

    @Override
    public void showCreateArchiveDialog(@NonNull FileItem file) {
        showCreateArchiveDialog(CollectionUtils.singletonLinkedSet(file));
    }

    @Override
    public void shareFile(@NonNull FileItem file) {
        shareFile(file.getPath(), file.getMimeType());
    }

    private void shareFile(@NonNull Path path, @NonNull String mimeType) {
        shareFiles(Collections.singletonList(path), Collections.singletonList(mimeType));
    }

    private void shareFiles(@NonNull List<Path> paths, @NonNull List<String> mimeTypes) {
        List<Uri> uris = Functional.map(paths, FileProvider::getUriForPath);
        Intent intent = IntentUtils.makeSendStream(uris, mimeTypes);
        AppUtils.startActivityWithChooser(intent, this);
    }

    @Override
    public void copyPath(@NonNull FileItem file) {
        copyPath(file.getPath());
    }

    @Override
    public void addBookmark(@NonNull FileItem file) {
        addBookmark(file.getPath());
    }

    private void addBookmark(@NonNull Path path) {
        BookmarkDirectories.add(new BookmarkDirectory(null, path));
        ToastUtils.show(R.string.file_add_bookmark_success, requireContext());
    }

    @Override
    public void showPropertiesDialog(@NonNull FileItem file) {
        FilePropertiesDialogFragment.show(file, this);
    }

    private void showCreateFileDialog() {
        CreateFileDialogFragment.show(this);
    }

    @Override
    public void createFile(@NonNull String name) {
        // TODO
        Path path = getCurrentPath().resolve(name);
        FileJobService.create(path, false, requireContext());
    }

    private void showCreateDirectoryDialog() {
        CreateDirectoryDialogFragment.show(this);
    }

    @Override
    public void createDirectory(@NonNull String name) {
        // TODO
        Path path = getCurrentPath().resolve(name);
        FileJobService.create(path, true, requireContext());
    }

    @NonNull
    @Override
    public Path getCurrentPath() {
        return mViewModel.getCurrentPath();
    }

    @Override
    public void navigateToRoot(@NonNull Path path) {
        collapseSearchView();
        mViewModel.resetTo(path);
    }

    @Override
    public void navigateToDefaultRoot() {
        navigateToRoot(Settings.FILE_LIST_DEFAULT_DIRECTORY.getValue());
    }

    @Override
    public void observeCurrentPath(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<Path> observer) {
        mViewModel.getCurrentPathLiveData().observe(owner, observer);
    }

    @Override
    public void closeNavigationDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}
