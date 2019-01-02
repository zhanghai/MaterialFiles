/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.provider.archive.ArchiveFileAttributes;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class FilePropertiesBasicTabFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesBasicTabFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FileItem";

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
    private FileItem mExtraFile;

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public FilePropertiesBasicTabFragment() {}

    @NonNull
    public static FilePropertiesBasicTabFragment newInstance(@NonNull FileItem fileItem) {
        //noinspection deprecation
        FilePropertiesBasicTabFragment fragment = new FilePropertiesBasicTabFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, fileItem);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_properties_basic_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNameText.setText(FileUtils.getName(mExtraFile));
        Path path = mExtraFile.getPath();
        if (LinuxFileSystemProvider.isLinuxPath(path)) {
            Path parentPath = path.getParent();
            if (parentPath != null) {
                ViewUtils.setVisibleOrGone(mParentDirectoryLayout, true);
                mParentDirectoryText.setText(parentPath.toString());
            }
        } else if (ArchiveFileSystemProvider.isArchivePath(path)) {
            ViewUtils.setVisibleOrGone(mArchiveFileAndEntryLayout, true);
            Path archiveFile = ArchiveFileSystemProvider.getArchiveFile(path);
            mArchiveFileText.setText(archiveFile.toFile().getPath());
            ArchiveFileAttributes attributes = (ArchiveFileAttributes) mExtraFile.getAttributes();
            mArchiveEntryText.setText(attributes.getEntryName());
        }
        mTypeText.setText(getTypeText(mExtraFile));
        boolean isSymbolicLink = mExtraFile.getAttributesNoFollowLinks().isSymbolicLink();
        ViewUtils.setVisibleOrGone(mSymbolicLinkTargetLayout, isSymbolicLink);
        if (isSymbolicLink) {
            mSymbolicLinkTargetText.setText(mExtraFile.getSymbolicLinkTarget());
        }
        mSizeText.setText(getSizeText(mExtraFile));
        String lastModificationTime = FormatUtils.formatLongTime(
                mExtraFile.getAttributes().lastModifiedTime().toInstant());
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
