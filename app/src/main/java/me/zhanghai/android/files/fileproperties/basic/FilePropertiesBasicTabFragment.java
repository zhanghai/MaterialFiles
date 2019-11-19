/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.basic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.fileproperties.FileData;
import me.zhanghai.android.files.fileproperties.FilePropertiesViewModel;
import me.zhanghai.android.files.provider.archive.ArchiveFileAttributes;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.ViewUtils;

public class FilePropertiesBasicTabFragment extends AppCompatDialogFragment {

    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorText;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.scroll)
    NestedScrollView mScrollView;
    @BindView(R.id.name)
    TextView mNameText;
    @BindView(R.id.parent_directory_layout)
    ViewGroup mParentDirectoryLayout;
    @BindView(R.id.parent_directory)
    TextView mParentDirectoryText;
    @BindView(R.id.archive_file_and_entry_layout)
    ViewGroup mArchiveFileAndEntryLayout;
    @BindView(R.id.archive_file)
    TextView mArchiveFileText;
    @BindView(R.id.archive_entry)
    TextView mArchiveEntryText;
    @BindView(R.id.type)
    TextView mTypeText;
    @BindView(R.id.symbolic_link_target_layout)
    ViewGroup mSymbolicLinkTargetLayout;
    @BindView(R.id.symbolic_link_target)
    TextView mSymbolicLinkTargetText;
    @BindView(R.id.size)
    TextView mSizeText;
    @BindView(R.id.last_modification_time)
    TextView mLastModificationTimeText;

    @NonNull
    private FilePropertiesViewModel mViewModel;

    private boolean mLastSuccess;

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public FilePropertiesBasicTabFragment() {}

    @NonNull
    public static FilePropertiesBasicTabFragment newInstance() {
        //noinspection deprecation
        return new FilePropertiesBasicTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(Settings.MATERIAL_DESIGN_2.getValue() ?
                R.layout.file_properties_basic_tab_fragment_md2
                : R.layout.file_properties_basic_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(getParentFragment()).get(FilePropertiesViewModel.class);

        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);

        mViewModel.getFileLiveData().observe(getViewLifecycleOwner(), this::onFileChanged);
    }

    private void refresh() {
        mViewModel.reloadFile();
    }

    private void onFileChanged(@NonNull FileData fileData) {
        switch (fileData.state) {
            case LOADING:
                ViewUtils.fadeToVisibility(mProgress, !mLastSuccess);
                if (mLastSuccess) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                ViewUtils.fadeOut(mErrorText);
                if (!mLastSuccess) {
                    ViewUtils.setVisibleOrInvisible(mScrollView, false);
                }
                break;
            case ERROR:
                ViewUtils.fadeOut(mProgress);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeIn(mErrorText);
                mErrorText.setText(fileData.exception.toString());
                ViewUtils.fadeOut(mScrollView);
                mLastSuccess = false;
                break;
            case SUCCESS: {
                ViewUtils.fadeOut(mProgress);
                mSwipeRefreshLayout.setRefreshing(false);
                ViewUtils.fadeOut(mErrorText);
                ViewUtils.fadeIn(mScrollView);
                updateView(fileData.data);
                mLastSuccess = true;
                break;
            }
            default:
                throw new AssertionError();
        }
    }

    private void updateView(@NonNull FileItem file) {
        mNameText.setText(FileUtils.getName(file));
        Path path = file.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path)
                || DocumentFileSystemProvider.isDocumentPath(path)) {
            Path parentPath = path.getParent();
            if (parentPath != null) {
                ViewUtils.setVisibleOrGone(mParentDirectoryLayout, true);
                mParentDirectoryText.setText(parentPath.toString());
            }
        } else if (ArchiveFileSystemProvider.isArchivePath(path)) {
            ViewUtils.setVisibleOrGone(mArchiveFileAndEntryLayout, true);
            Path archiveFile = ArchiveFileSystemProvider.getArchiveFile(path);
            mArchiveFileText.setText(archiveFile.toFile().getPath());
            ArchiveFileAttributes attributes = (ArchiveFileAttributes) file.getAttributes();
            mArchiveEntryText.setText(attributes.getEntryName());
        }
        mTypeText.setText(getTypeText(file));
        boolean isSymbolicLink = file.getAttributesNoFollowLinks().isSymbolicLink();
        ViewUtils.setVisibleOrGone(mSymbolicLinkTargetLayout, isSymbolicLink);
        if (isSymbolicLink) {
            mSymbolicLinkTargetText.setText(file.getSymbolicLinkTarget());
        }
        mSizeText.setText(getSizeText(file));
        String lastModificationTime = FormatUtils.formatLongTime(
                file.getAttributes().lastModifiedTime().toInstant());
        mLastModificationTimeText.setText(lastModificationTime);
    }

    @NonNull
    private String getTypeText(@NonNull FileItem fileItem) {
        int typeFormatRes = fileItem.getAttributesNoFollowLinks().isSymbolicLink()
                && !fileItem.isSymbolicLinkBroken() ?
                R.string.file_properties_basic_type_symbolic_link_format
                : R.string.file_properties_basic_type_format;
        String typeName = FileUtils.getTypeName(fileItem, requireContext());
        String mimeType = fileItem.getMimeType();
        return getString(typeFormatRes, typeName, mimeType);
    }

    @NonNull
    private String getSizeText(@NonNull FileItem fileItem) {
        long size = fileItem.getAttributes().size();
        String sizeInBytes = FormatUtils.formatSizeInBytes(size, mSizeText.getContext());
        if (FormatUtils.isHumanReadableSizeInBytes(size)) {
            return sizeInBytes;
        } else {
            String humanReadableSize = FormatUtils.formatHumanReadableSize(size,
                    mSizeText.getContext());
            return getString(R.string.file_properties_basic_size_with_human_readable_format,
                    humanReadableSize, sizeInBytes);
        }
    }
}
