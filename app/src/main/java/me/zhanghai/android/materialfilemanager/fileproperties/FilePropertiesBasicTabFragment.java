/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.fileproperties;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.FormatUtils;
import me.zhanghai.android.materialfilemanager.filesystem.ArchiveFile;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FilePropertiesBasicTabFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesBasicTabFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

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
    private File mExtraFile;

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public FilePropertiesBasicTabFragment() {}

    @NonNull
    public static FilePropertiesBasicTabFragment newInstance(@NonNull File file) {
        //noinspection deprecation
        FilePropertiesBasicTabFragment fragment = new FilePropertiesBasicTabFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
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

        mNameText.setText(mExtraFile.getName());
        if (mExtraFile instanceof LocalFile) {
            LocalFile file = (LocalFile) mExtraFile;
            LocalFile parentFile = file.getParent();
            if (parentFile != null) {
                ViewUtils.setVisibleOrGone(mParentDirectoryLayout, true);
                mParentDirectoryText.setText(parentFile.getPath());
            }
        } else if (mExtraFile instanceof ArchiveFile) {
            ViewUtils.setVisibleOrGone(mArchiveFileAndEntryLayout, true);
            ArchiveFile file = (ArchiveFile) mExtraFile;
            mArchiveFileText.setText(file.getArchiveFile().getPath());
            mArchiveEntryText.setText(file.getEntryName());
        }
        mTypeText.setText(getTypeText(mExtraFile));
        boolean isSymbolicLink = mExtraFile.isSymbolicLink();
        ViewUtils.setVisibleOrGone(mSymbolicLinkTargetLayout, isSymbolicLink);
        if (isSymbolicLink) {
            mSymbolicLinkTargetText.setText(mExtraFile.getSymbolicLinkTarget());
        }
        mSizeText.setText(getSizeText(mExtraFile));
        String lastModificationTime = FormatUtils.formatLongTime(mExtraFile.getLastModificationTime());
        mLastModificationTimeText.setText(lastModificationTime);
    }

    @NonNull
    private String getTypeText(@NonNull File file) {
        int typeFormatRes = file.isSymbolicLink() && !file.isSymbolicLinkBroken() ?
                R.string.file_properties_basic_type_symbolic_link_format
                : R.string.file_properties_basic_type_format;
        String typeName = file.getTypeName(requireContext());
        String mimeType = file.getMimeType();
        return getString(typeFormatRes, typeName, mimeType);
    }

    @NonNull
    private String getSizeText(@NonNull File file) {
        long size = file.getSize();
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
