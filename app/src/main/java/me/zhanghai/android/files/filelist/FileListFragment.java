/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java8.nio.file.Paths;
import me.zhanghai.android.effortlesspermissions.AfterPermissionDenied;
import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.effortlesspermissions.OpenAppDetailsDialogFragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.fileproperties.FilePropertiesDialogFragment;
import me.zhanghai.android.files.main.MainActivity;
import me.zhanghai.android.files.navigation.NavigationFragment;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.settings.SettingsLiveDatas;
import me.zhanghai.android.files.terminal.Terminal;
import me.zhanghai.android.files.ui.ToolbarActionMode;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.ClipboardUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;
import me.zhanghai.android.files.util.ViewUtils;
import me.zhanghai.android.files.viewer.image.ImageViewerActivity;
import me.zhanghai.java.functional.Functional;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class FileListFragment extends Fragment implements BreadcrumbLayout.Listener,
        FileListAdapter.Listener, ToolbarActionMode.Callback, OpenApkDialogFragment.Listener,
        OpenFileAsDialogFragment.Listener, ConfirmDeleteFilesDialogFragment.Listener,
        CreateArchiveDialogFragment.Listener, RenameFileDialogFragment.Listener,
        CreateFileDialogFragment.Listener, CreateDirectoryDialogFragment.Listener,
        NavigationFragment.FileListListener {

    private static final int REQUEST_CODE_STORAGE_PERMISSIONS = 1;

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Intent mIntent;
    @Nullable
    private Path mExtraPath;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.action_mode_toolbar)
    Toolbar mActionModeToolbar;
    @BindView(R.id.breadcrumb)
    BreadcrumbLayout mBreadcrumbLayout;
    @BindView(R.id.content)
    ViewGroup mContentLayout;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorView;
    @BindView(R.id.empty)
    View mEmptyView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.speed_dial)
    SpeedDialView mSpeedDialView;

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
    private MenuItem mShowHiddenFilesMenuItem;

    @NonNull
    private ToolbarActionMode mToolbarActionMode;

    @NonNull
    private FileListAdapter mAdapter;

    @NonNull
    private FileListViewModel mViewModel;

    @Nullable
    private Path mLastFile;

    public static void putArguments(@NonNull Intent intent, @Nullable Path path) {
        if (path != null) {
            IntentPathUtils.putExtraPath(intent, path);
        }
    }

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

        mIntent = getArguments().getParcelable(Intent.EXTRA_INTENT);
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

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mToolbarActionMode = new ToolbarActionMode(mActionModeToolbar);
        if (savedInstanceState != null) {
            mToolbarActionMode.restoreInstanceState(savedInstanceState, this);
        }

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
            Path path;
            if (mExtraPath != null) {
                path = mExtraPath;
                String mimeType = mIntent.getType();
                if (mimeType != null && FileUtils.isArchiveFile(path, mimeType)) {
                    path = ArchiveFileSystemProvider.getRootPathForArchiveFile(path);
                }
            } else {
                // TODO: Allow configuration.
                path = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
            mViewModel.resetTo(path);
        }
        mViewModel.getCurrentPathLiveData().observe(this, this::onCurrentPathChanged);
        mViewModel.getBreadcrumbLiveData().observe(this, mBreadcrumbLayout::setData);
        FileSortOptionsLiveData.getInstance().observe(this, this::onSortOptionsChanged);
        mViewModel.getSelectedFilesLiveData().observe(this, this::onSelectedFilesChanged);
        mViewModel.getPasteModeLiveData().observe(this, this::onPasteModeChanged);
        mViewModel.getFileListLiveData().observe(this, this::onFileListChanged);
        SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.observe(this, this::onShowHiddenFilesChanged);

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mToolbarActionMode.saveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.file_list, menu);
        mSortByNameMenuItem = menu.findItem(R.id.action_sort_by_name);
        mSortByTypeMenuItem = menu.findItem(R.id.action_sort_by_type);
        mSortBySizeMenuItem = menu.findItem(R.id.action_sort_by_size);
        mSortByLastModifiedMenuItem = menu.findItem(R.id.action_sort_by_last_modified);
        mSortOrderAscendingMenuItem = menu.findItem(R.id.action_sort_order_ascending);
        mSortDirectoriesFirstMenuItem = menu.findItem(R.id.action_sort_directories_first);
        mShowHiddenFilesMenuItem = menu.findItem(R.id.action_show_hidden_files);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        updateSortOptionsMenuItems();
        updateShowHiddenFilesMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
            case R.id.action_send:
                send();
                return true;
            case R.id.action_copy_path:
                copyPath();
                return true;
            case R.id.action_open_in_terminal:
                openInTerminal();
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
        if (mToolbarActionMode.isActive()) {
            mToolbarActionMode.finish();
            return true;
        }
        return mViewModel.navigateUp(false);
    }

    private void onCurrentPathChanged(@NonNull Path path) {
        updateCab();
    }

    private void onFileListChanged(@NonNull FileListData fileListData) {
        switch (fileListData.state) {
            case LOADING: {
                Path path = fileListData.path;
                boolean isReload = Objects.equals(path, mLastFile);
                mLastFile = path;
                if (!isReload) {
                    mToolbar.setSubtitle(R.string.file_list_subtitle_loading);
                    ViewUtils.fadeIn(mProgress);
                    ViewUtils.fadeOut(mErrorView);
                    ViewUtils.fadeOut(mEmptyView);
                    mAdapter.clear();
                }
                break;
            }
            case ERROR:
                fileListData.exception.printStackTrace();
                mToolbar.setSubtitle(R.string.file_list_subtitle_error);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorView);
                mErrorView.setText(fileListData.exception.toString());
                ViewUtils.fadeOut(mEmptyView);
                mAdapter.clear();
                break;
            case SUCCESS: {
                List<FileItem> fileList = fileListData.fileList;
                updateSubtitle(fileList);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeToVisibility(mEmptyView, fileList.isEmpty());
                updateAdapterFileList();
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
            subtitle = getString(R.string.file_list_subtitle_empty);
        }
        mToolbar.setSubtitle(subtitle);
    }

    private void setSortBy(@NonNull FileSortOptions.By by) {
        FileSortOptionsLiveData.getInstance().putBy(by);
    }

    private void setSortOrder(@NonNull FileSortOptions.Order order) {
        FileSortOptionsLiveData.getInstance().putOrder(order);
    }

    private void setSortDirectoriesFirst(boolean directoriesFirst) {
        FileSortOptionsLiveData.getInstance().putDirectoriesFirst(directoriesFirst);
    }

    private void onSortOptionsChanged(@NonNull FileSortOptions sortOptions) {
        mAdapter.setComparator(sortOptions.makeComparator());
        updateSortOptionsMenuItems();
    }

    private void updateSortOptionsMenuItems() {
        if (mSortByNameMenuItem == null || mSortByTypeMenuItem == null
                || mSortBySizeMenuItem == null || mSortByLastModifiedMenuItem == null
                || mSortOrderAscendingMenuItem == null || mSortDirectoriesFirstMenuItem == null) {
            return;
        }
        MenuItem checkedSortByMenuItem;
        FileSortOptions sortOptions = FileSortOptionsLiveData.getInstance().getValue();
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

    private void navigateUp() {
        mViewModel.navigateUp(true);
    }

    private void newTask() {
        openInNewTask(getCurrentPath());
    }

    private void refresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mViewModel.reload();
    }

    private void setShowHiddenFiles(boolean showHiddenFiles) {
        SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.putValue(showHiddenFiles);
    }

    private void onShowHiddenFilesChanged(boolean showHiddenFiles) {
        updateAdapterFileList();
        updateShowHiddenFilesMenuItem();
    }

    private void updateAdapterFileList() {
        FileListData fileListData = mViewModel.getFileListData();
        if (fileListData.fileList == null) {
            return;
        }
        List<FileItem> files = fileListData.fileList;
        if (!SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.getValue()) {
            files = Functional.filter(files, file -> !file.isHidden());
        }
        mAdapter.replace(files);
    }

    private void updateShowHiddenFilesMenuItem() {
        if (mShowHiddenFilesMenuItem == null) {
            return;
        }
        boolean showHiddenFiles = SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.getValue();
        mShowHiddenFilesMenuItem.setChecked(showHiddenFiles);
    }

    private void send() {
        sendFile(getCurrentPath(), MimeTypes.DIRECTORY_MIME_TYPE);
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

    @Override
    public void navigateTo(@NonNull Path path) {
        Parcelable state = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mViewModel.navigateTo(state, path);
    }

    @Override
    public void copyPath(@NonNull Path path) {
        String pathString;
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            pathString = path.toFile().getPath();
        } else {
            pathString = path.toUri().toString();
        }
        ClipboardUtils.copyText(pathString, requireContext());
    }

    @Override
    public void openInNewTask(@NonNull Path path) {
        Intent intent = MainActivity.newIntent(path, requireContext())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    @Override
    public void selectFile(@NonNull FileItem file, boolean selected) {
        mViewModel.selectFile(file, selected);
    }

    private void onSelectedFilesChanged(@NonNull Set<FileItem> files) {
        mAdapter.replaceSelectedFiles(files);
        updateCab();
    }

    private void updateCab() {
        Set<FileItem> selectedFiles = mViewModel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            if (mToolbarActionMode.isActive()) {
                mToolbarActionMode.finish();
            }
            return;
        }
        FilePasteMode pasteMode = mViewModel.getPasteMode();
        boolean isExtract = Functional.every(selectedFiles, file ->
                ArchiveFileSystemProvider.isArchivePath(file.getPath()));
        int titleRes;
        int menuRes;
        switch (pasteMode) {
            case NONE:
                titleRes = R.string.file_list_cab_select_title_format;
                menuRes = R.menu.file_list_cab_select;
                break;
            case MOVE:
                titleRes = R.string.file_list_cab_paste_move_title_format;
                menuRes = R.menu.file_list_cab_paste;
                break;
            case COPY:
                titleRes = isExtract ? R.string.file_list_cab_paste_extract_title_format
                        : R.string.file_list_cab_paste_copy_title_format;
                menuRes = R.menu.file_list_cab_paste;
                break;
            default:
                throw new IllegalStateException();
        }
        mToolbarActionMode.setTitle(getString(titleRes, selectedFiles.size()));
        mToolbarActionMode.setMenuResource(menuRes);
        Menu menu = mToolbarActionMode.getMenu();
        switch (pasteMode) {
            case NONE: {
                boolean hasReadOnly = Functional.some(selectedFiles, file ->
                        file.getPath().getFileSystem().isReadOnly());
                menu.findItem(R.id.action_cut).setVisible(!hasReadOnly);
                menu.findItem(R.id.action_copy)
                        .setIcon(isExtract ? R.drawable.extract_icon_white_24dp
                                : R.drawable.copy_icon_white_24dp)
                        .setTitle(isExtract ? R.string.file_list_cab_select_action_extract
                                : R.string.file_list_cab_select_action_copy);
                menu.findItem(R.id.action_delete).setVisible(!hasReadOnly);
                break;
            }
            case MOVE:
            case COPY: {
                boolean isReadOnly = mViewModel.getCurrentPath().getFileSystem().isReadOnly();
                menu.findItem(R.id.action_paste)
                        .setTitle(isExtract ? R.string.file_list_cab_paste_action_extract_here
                                : R.string.file_list_cab_paste_action_paste)
                        .setEnabled(!isReadOnly);
                break;
            }
            default:
                throw new IllegalStateException();
        }
        if (!mToolbarActionMode.isActive()) {
            mAppBarLayout.setExpanded(true);
            mToolbarActionMode.start(this);
        }
    }

    @Override
    public void onToolbarActionModeStarted(@NonNull ToolbarActionMode toolbarActionMode) {}

    @Override
    public boolean onToolbarActionModeItemClicked(@NonNull ToolbarActionMode toolbarActionMode,
                                                  @NonNull MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.action_select_all:
                selectAllFiles();
                return true;
            case R.id.action_paste:
                pasteFiles(mViewModel.getSelectedFiles(), getCurrentPath());
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onToolbarActionModeFinished(@NonNull ToolbarActionMode toolbarActionMode) {
        mViewModel.clearSelectedFiles();
        mViewModel.setPasteMode(FilePasteMode.NONE);
    }

    private void cutFiles(@NonNull Set<FileItem> files) {
        if (mViewModel.getPasteMode() == FilePasteMode.MOVE) {
            mViewModel.selectFiles(files, true);
        } else {
            mViewModel.replaceSelectedFiles(files);
            mViewModel.setPasteMode(FilePasteMode.MOVE);
        }
    }

    private void copyFiles(@NonNull Set<FileItem> files) {
        if (mViewModel.getPasteMode() == FilePasteMode.COPY) {
            mViewModel.selectFiles(files, true);
        } else {
            mViewModel.replaceSelectedFiles(files);
            mViewModel.setPasteMode(FilePasteMode.COPY);
        }
    }

    private void onPasteModeChanged(@NonNull FilePasteMode pasteMode) {
        mAdapter.setPasteMode(pasteMode);
        updateCab();
    }

    private void pasteFiles(@NonNull Set<FileItem> sources, @NonNull Path targetDirectory) {
        switch (mViewModel.getPasteMode()) {
            case MOVE:
                FileJobService.move(makePathListForJob(sources), targetDirectory, requireContext());
                break;
            case COPY:
                FileJobService.copy(makePathListForJob(sources), targetDirectory, requireContext());
                break;
            default:
                throw new IllegalStateException();
        }
        mViewModel.selectFiles(sources, false);
        mViewModel.setPasteMode(FilePasteMode.NONE);
    }

    private void confirmDeleteFiles(@NonNull Set<FileItem> files) {
        ConfirmDeleteFilesDialogFragment.show(files, this);
    }

    @Override
    public void deleteFiles(@NonNull Set<FileItem> files) {
        mViewModel.selectFiles(files, false);
        FileJobService.delete(makePathListForJob(files), requireContext());
    }

    private void showCreateArchiveDialog(@NonNull Set<FileItem> files) {
        CreateArchiveDialogFragment.show(files, this);
    }

    @Override
    public void archive(@NonNull Set<FileItem> files, @NonNull String name,
                        @NonNull String archiveType, @Nullable String compressorType) {
        mViewModel.selectFiles(files, false);
        Path archiveFile = mViewModel.getCurrentPath().resolve(name);
        FileJobService.archive(makePathListForJob(files), archiveFile, archiveType, compressorType,
                requireContext());
    }

    @NonNull
    private List<Path> makePathListForJob(@NonNull Set<FileItem> files) {
        List<Path> pathList = Functional.map(files, FileItem::getPath);
        Collections.sort(pathList);
        return pathList;
    }

    private void selectAllFiles() {
        mViewModel.selectAllFiles();
    }

    @Override
    public void openFile(@NonNull FileItem file) {
        String mimeType = file.getMimeType();
        if (MimeTypes.isApk(mimeType)) {
            openApk(file);
            return;
        }
        if (FileUtils.isListable(file)) {
            navigateTo(FileUtils.toListablePath(file));
            return;
        }
        openFileAs(file, mimeType);
    }

    private void openApk(@NonNull FileItem file) {
        switch (SettingsLiveDatas.OPEN_APK_DEFAULT_ACTION.getValue()) {
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
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void installApk(@NonNull FileItem file) {
        openFileAs(file, file.getMimeType());
    }

    @Override
    public void viewApk(@NonNull FileItem file) {
        navigateTo(FileUtils.toListablePath(file));
    }

    @Override
    public void showOpenFileAsDialog(@NonNull FileItem file) {
        OpenFileAsDialogFragment.show(file, this);
    }

    @Override
    public void openFileAs(@NonNull FileItem file, @NonNull String mimeType) {
        Path path = file.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            Uri uri = FileProvider.getUriForPath(path);
            Intent intent = IntentUtils.makeView(uri, mimeType)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            IntentPathUtils.putExtraPath(intent, path);
            maybeAddImageViewerActivityExtras(intent, file, mimeType);
            AppUtils.startActivity(intent, this);
        } else {
            FileJobService.open(path, mimeType, requireContext());
        }
    }

    private void maybeAddImageViewerActivityExtras(@NonNull Intent intent, @NonNull FileItem file,
                                                   @NonNull String mimeType) {
        if (!MimeTypes.isImage(mimeType) || !MimeTypes.isImage(file.getMimeType())) {
            return;
        }
        List<Path> paths = new ArrayList<>();
        // We need the ordered list from our adapter instead of the list from FileListLiveData.
        for (int i = 0; i < mAdapter.getItemCount(); ++i) {
            FileItem adapterFile = mAdapter.getItem(i);
            if (!MimeTypes.isImage(adapterFile.getMimeType())) {
                continue;
            }
            paths.add(adapterFile.getPath());
        }
        int position = paths.indexOf(file.getPath());
        if (position == -1) {
            return;
        }
        ImageViewerActivity.putExtras(intent, paths, position);
    }

    @Override
    public void cutFile(@NonNull FileItem file) {
        cutFiles(Collections.singleton(file));
    }

    @Override
    public void copyFile(@NonNull FileItem file) {
        copyFiles(Collections.singleton(file));
    }

    @Override
    public void confirmDeleteFile(@NonNull FileItem file) {
        confirmDeleteFiles(Collections.singleton(file));
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
        return Functional.some(fileListData.fileList, path -> Objects.equals(FileUtils.getName(
                path), name));
    }

    @Override
    public void renameFile(@NonNull FileItem file, @NonNull String newName) {
        FileJobService.rename(file.getPath(), newName, requireContext());
    }

    @Override
    public void extractFile(@NonNull FileItem file) {
        copyFiles(Collections.singleton(FileUtils.createDummyFileItemForArchiveRoot(file)));
    }

    @Override
    public void showCreateArchiveDialog(@NonNull FileItem file) {
        showCreateArchiveDialog(Collections.singleton(file));
    }

    @Override
    public void sendFile(@NonNull FileItem file) {
        sendFile(file.getPath(), file.getMimeType());
    }

    private void sendFile(@NonNull Path path, @NonNull String mimeType) {
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            Uri uri = FileProvider.getUriForPath(path);
            Intent intent = IntentUtils.makeSendStream(uri, mimeType);
            AppUtils.startActivityWithChooser(intent, this);
        } else {
            // TODO
        }
    }

    @Override
    public void copyPath(@NonNull FileItem file) {
        copyPath(file.getPath());
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
        FileJobService.createFile(path, requireContext());
    }

    private void showCreateDirectoryDialog() {
        CreateDirectoryDialogFragment.show(this);
    }

    @Override
    public void createDirectory(@NonNull String name) {
        // TODO
        Path path = getCurrentPath().resolve(name);
        FileJobService.createDirectory(path, requireContext());
    }

    @NonNull
    @Override
    public Path getCurrentPath() {
        return mViewModel.getCurrentPath();
    }

    @Override
    public void navigateToRoot(@NonNull Path path) {
        mViewModel.resetTo(path);
    }

    @Override
    public void observeCurrentPath(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<Path> observer) {
        mViewModel.getCurrentPathLiveData().observe(owner, observer);
    }
}
