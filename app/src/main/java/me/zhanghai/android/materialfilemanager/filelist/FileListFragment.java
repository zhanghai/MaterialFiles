/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.Manifest;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.afollestad.materialcab.MaterialCab;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.effortlesspermissions.AfterPermissionDenied;
import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.effortlesspermissions.OpenAppDetailsDialogFragment;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.FileJobService;
import me.zhanghai.android.materialfilemanager.file.FileProvider;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.fileproperties.FilePropertiesDialogFragment;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.FileSystemException;
import me.zhanghai.android.materialfilemanager.filesystem.Files;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.main.MainActivity;
import me.zhanghai.android.materialfilemanager.navigation.NavigationFragment;
import me.zhanghai.android.materialfilemanager.settings.SettingsLiveDatas;
import me.zhanghai.android.materialfilemanager.shell.SuShellHelperFragment;
import me.zhanghai.android.materialfilemanager.terminal.Terminal;
import me.zhanghai.android.materialfilemanager.ui.SetMenuResourceMaterialCab;
import me.zhanghai.android.materialfilemanager.util.AppUtils;
import me.zhanghai.android.materialfilemanager.util.ClipboardUtils;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.IntentUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class FileListFragment extends Fragment implements BreadcrumbLayout.Listener,
        FileListAdapter.Listener, MaterialCab.Callback, OpenFileAsDialogFragment.Listener,
        ConfirmDeleteFilesDialogFragment.Listener, RenameFileDialogFragment.Listener,
        CreateFileDialogFragment.Listener, CreateDirectoryDialogFragment.Listener,
        NavigationFragment.FileListListener {

    private static final String KEY_PREFIX = FileListFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    @Nullable
    private File mExtraFile;

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
    private SetMenuResourceMaterialCab mCab;

    @NonNull
    private FileListAdapter mAdapter;

    @NonNull
    private FileListViewModel mViewModel;

    @Nullable
    private File mLastFile;

    @NonNull
    public static FileListFragment newInstance(@Nullable File file) {
        //noinspection deprecation
        FileListFragment fragment = new FileListFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public FileListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);

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

        SuShellHelperFragment.attachToActivity(this);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {
            mCab = new SetMenuResourceMaterialCab(activity, R.id.cab_stub);
        } else {
            mCab = SetMenuResourceMaterialCab.restoreState(savedInstanceState, activity, this);
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
            File file;
            if (mExtraFile != null) {
                file = mExtraFile;
            } else {
                // TODO: Allow configuration.
                file = Files.ofLocalPath(
                        Environment.getExternalStorageDirectory().getAbsolutePath());
            }
            mViewModel.resetTo(file);
        }
        mViewModel.getBreadcrumbLiveData().observe(this, mBreadcrumbLayout::setData);
        mViewModel.getSortOptionsLiveData().observe(this, this::onSortOptionsChanged);
        mViewModel.getSelectedFilesLiveData().observe(this, this::onSelectedFilesChanged);
        mViewModel.getPasteModeLiveData().observe(this, this::onPasteModeChanged);
        mViewModel.getFileListLiveData().observe(this, this::onFileListChanged);
        SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.observe(this, this::onShowHiddenFilesChanged);

        if (!EffortlessPermissions.hasPermissions(this, PERMISSIONS_STORAGE)) {
            EffortlessPermissions.requestPermissions(this,
                    R.string.storage_permission_request_message, REQUEST_CODE_STORAGE_PERMISSION,
                    PERMISSIONS_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EffortlessPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                this);
    }

    @AfterPermissionGranted(REQUEST_CODE_STORAGE_PERMISSION)
    private void onStoragePermissionGranted() {
        mViewModel.reload();
    }

    @AfterPermissionDenied(REQUEST_CODE_STORAGE_PERMISSION)
    private void onStoragePermissionDenied() {
        if (EffortlessPermissions.somePermissionPermanentlyDenied(this, PERMISSIONS_STORAGE)) {
            OpenAppDetailsDialogFragment.show(
                    R.string.storage_permission_permanently_denied_message,
                    R.string.open_settings, this);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mCab.saveState(outState);
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

        updateSortOptionsMenu();
        updateShowHiddenFilesMenu();
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
        return mViewModel.navigateUp(false);
    }

    private void onFileListChanged(@NonNull FileListData fileListData) {
        switch (fileListData.state) {
            case LOADING: {
                File file = fileListData.file;
                boolean isReload = Objects.equals(file, mLastFile);
                mLastFile = file;
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
                mToolbar.setSubtitle(R.string.file_list_subtitle_error);
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
                mAdapter.clear();
                break;
            case SUCCESS: {
                List<File> fileList = fileListData.fileList;
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

    private void updateSubtitle(@NonNull List<File> files) {
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

    private void setSortBy(@NonNull FileSortOptions.By by) {
        FileSortOptions sortOptions = mViewModel.getSortOptions();
        if (sortOptions.getBy() == by) {
            return;
        }
        mViewModel.setSortOptions(sortOptions.withBy(by));
    }

    private void setSortOrder(@NonNull FileSortOptions.Order order) {
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

    private void onSortOptionsChanged(@NonNull FileSortOptions sortOptions) {
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

    private void navigateUp() {
        mViewModel.navigateUp(true);
    }

    private void newTask() {
        openInNewTask(getCurrentFile());
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
        updateShowHiddenFilesMenu();
    }

    private void updateAdapterFileList() {
        FileListData fileListData = mViewModel.getFileListData();
        if (fileListData.fileList == null) {
            return;
        }
        List<File> fileList = fileListData.fileList;
        if (!SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.getValue()) {
            fileList = Functional.filter(fileList, file -> !file.isHidden());
        }
        mAdapter.replace(fileList);
    }

    private void updateShowHiddenFilesMenu() {
        if (mShowHiddenFilesMenuItem == null) {
            return;
        }
        boolean showHiddenFiles = SettingsLiveDatas.FILE_LIST_SHOW_HIDDEN_FILES.getValue();
        mShowHiddenFilesMenuItem.setChecked(showHiddenFiles);
    }

    private void send() {
        sendFile(getCurrentFile(), MimeTypes.DIRECTORY_MIME_TYPE);
    }

    private void copyPath() {
        copyPath(getCurrentFile());
    }

    private void openInTerminal() {
        File file = getCurrentFile();
        if (file instanceof LocalFile) {
            LocalFile localFile = (LocalFile) file;
            Terminal.open(localFile.getPath(), requireContext());
        } else {
            // TODO
        }
    }

    @Override
    public void navigateToFile(@NonNull File file) {
        Parcelable state = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mViewModel.navigateTo(state, file);
    }

    @Override
    public void copyPath(@NonNull File file) {
        String path;
        if (file instanceof LocalFile) {
            LocalFile localFile = (LocalFile) file;
            path = localFile.getPath();
        } else {
            path = file.getUri().toString();
        }
        ClipboardUtils.copyText(path, requireContext());
    }

    @Override
    public void openInNewTask(@NonNull File file) {
        Intent intent = MainActivity.makeIntent(file, requireContext())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    @Override
    public void selectFile(@NonNull File file, boolean selected) {
        mViewModel.selectFile(file, selected);
    }

    private void onSelectedFilesChanged(@NonNull Set<File> selectedFiles) {
        mAdapter.replaceSelectedFiles(selectedFiles);
        updateCab();
    }

    private void updateCab() {
        Set<File> selectedFiles = mViewModel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            if (mCab.isActive()) {
                mCab.finish();
            }
            return;
        }
        FilePasteMode pasteMode = mViewModel.getPasteMode();
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
                titleRes = R.string.file_list_cab_paste_copy_title_format;
                menuRes = R.menu.file_list_cab_paste;
                break;
            default:
                throw new IllegalStateException();
        }
        mCab.setTitle(getString(titleRes, selectedFiles.size()));
        mCab.setMenuResource(menuRes);
        if (!mCab.isActive()) {
            mCab.start(this);
        }
    }

    @Override
    public boolean onCabCreated(@NonNull MaterialCab cab, @NonNull Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(@NonNull MenuItem item) {
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
            case R.id.action_select_all:
                selectAllFiles();
                return true;
            case R.id.action_paste:
                pasteFiles(mViewModel.getSelectedFiles(), getCurrentFile());
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCabFinished(@NonNull MaterialCab cab) {
        mViewModel.clearSelectedFiles();
        mViewModel.setPasteMode(FilePasteMode.NONE);
        return true;
    }

    private void cutFiles(@NonNull Set<File> files) {
        if (mViewModel.getPasteMode() == FilePasteMode.MOVE) {
            mViewModel.selectFiles(files, true);
        } else {
            mViewModel.replaceSelectedFiles(files);
            mViewModel.setPasteMode(FilePasteMode.MOVE);
        }
    }

    private void copyFiles(@NonNull Set<File> files) {
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

    private void pasteFiles(@NonNull Set<File> fromFiles, @NonNull File toDirectory) {
        switch (mViewModel.getPasteMode()) {
            case MOVE:
                FileJobService.move(makeFileListForJob(fromFiles), toDirectory, requireContext());
                break;
            case COPY:
                FileJobService.copy(makeFileListForJob(fromFiles), toDirectory, requireContext());
                break;
            default:
                throw new IllegalStateException();
        }
        mViewModel.selectFiles(fromFiles, false);
        mViewModel.setPasteMode(FilePasteMode.NONE);
    }

    private void confirmDeleteFiles(@NonNull Set<File> files) {
        ConfirmDeleteFilesDialogFragment.show(files, this);
    }

    @Override
    public void deleteFiles(@NonNull Set<File> files) {
        mViewModel.selectFiles(files, false);
        FileJobService.delete(makeFileListForJob(files), requireContext());
    }

    @NonNull
    private List<File> makeFileListForJob(@NonNull Set<File> files) {
        List<File> fileList = new ArrayList<>(files);
        Collections.sort(fileList);
        return fileList;
    }

    private void selectAllFiles() {
        mViewModel.selectAllFiles();
    }

    @Override
    public void openFile(@NonNull File file) {
        if (file.isListable()) {
            navigateToFile(file.asListableFile());
            return;
        }
        openFileAs(file, file.getMimeType());
    }

    @Override
    public void showOpenFileAsDialog(@NonNull File file) {
        OpenFileAsDialogFragment.show(file, this);
    }

    @Override
    public void openFileAs(@NonNull File file, @NonNull String mimeType) {
        if (file instanceof LocalFile) {
            LocalFile localFile = (LocalFile) file;
            Uri fileUri = FileProvider.getUriForPath(localFile.getPath());
            Intent intent = IntentUtils.makeView(fileUri, mimeType)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            AppUtils.startActivity(intent, requireContext());
        } else {
            // TODO
        }
    }

    @Override
    public void cutFile(@NonNull File file) {
        cutFiles(Collections.singleton(file));
    }

    @Override
    public void copyFile(@NonNull File file) {
        copyFiles(Collections.singleton(file));
    }

    @Override
    public void confirmDeleteFile(@NonNull File file) {
        confirmDeleteFiles(Collections.singleton(file));
    }

    @Override
    public void showRenameFileDialog(@NonNull File file) {
        RenameFileDialogFragment.show(file, this);
    }

    @Override
    public boolean hasFileWithName(@NonNull String name) {
        FileListData fileListData = mViewModel.getFileListLiveData().getValue();
        if (fileListData == null || fileListData.state != FileListData.State.SUCCESS) {
            return false;
        }
        return Functional.some(fileListData.fileList, file -> TextUtils.equals(file.getName(),
                name));
    }

    @Override
    public void renameFile(@NonNull File file, @NonNull String name) {
        // TODO
        FileJobService.rename(file, name, requireContext());
    }

    @Override
    public void sendFile(@NonNull File file) {
        sendFile(file, file.getMimeType());
    }

    private void sendFile(@NonNull File file, @NonNull String mimeType) {
        if (file instanceof LocalFile) {
            LocalFile localFile = (LocalFile) file;
            Uri fileUri = FileProvider.getUriForPath(localFile.getPath());
            Intent intent = IntentUtils.makeSendStream(fileUri, mimeType);
            AppUtils.startActivityWithChooser(intent, requireContext());
        } else {
            // TODO
        }
    }

    @Override
    public void showPropertiesDialog(@NonNull File file) {
        FilePropertiesDialogFragment.show(file, this);
    }

    private void showCreateFileDialog() {
        CreateFileDialogFragment.show(this);
    }

    @Override
    public void createFile(@NonNull String name) {
        // TODO
        File file = Files.childOf(getCurrentFile(), name);
        FileJobService.createFile(file, requireContext());
    }

    private void showCreateDirectoryDialog() {
        CreateDirectoryDialogFragment.show(this);
    }

    @Override
    public void createDirectory(@NonNull String name) {
        // TODO
        File file = Files.childOf(getCurrentFile(), name);
        FileJobService.createDirectory(file, requireContext());
    }

    @NonNull
    @Override
    public File getCurrentFile() {
        return mViewModel.getCurrentFile();
    }

    @Override
    public void navigateToRoot(@NonNull File file) {
        mViewModel.resetTo(file);
    }

    @Override
    public void observeCurrentFile(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<File> observer) {
        mViewModel.getCurrentFileLiveData().observe(owner, observer);
    }
}
