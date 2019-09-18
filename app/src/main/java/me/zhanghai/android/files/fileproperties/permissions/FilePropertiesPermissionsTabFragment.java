/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.fileproperties.FileData;
import me.zhanghai.android.files.fileproperties.FilePropertiesViewModel;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.ObjectUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class FilePropertiesPermissionsTabFragment extends AppCompatDialogFragment {

    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorText;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.scroll)
    NestedScrollView mScrollView;
    @BindView(R.id.owner)
    TextView mOwnerText;
    @BindView(R.id.group)
    TextView mGroupText;
    @BindView(R.id.mode)
    TextView mModeText;
    @BindView(R.id.selinux_context_layout)
    ViewGroup mSeLinuxContextLayout;
    @BindView(R.id.selinux_context)
    TextView mSeLinuxContextText;

    @NonNull
    private FilePropertiesViewModel mViewModel;

    private boolean mLastSuccess;

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public FilePropertiesPermissionsTabFragment() {}

    public static boolean isAvailable(@NonNull FileItem file) {
        BasicFileAttributes attributes = file.getAttributes();
        if (attributes instanceof PosixFileAttributes) {
            PosixFileAttributes posixAttributes = (PosixFileAttributes) attributes;
            return posixAttributes.owner() != null || posixAttributes.group() != null
                    || posixAttributes.mode() != null;
        }
        return false;
    }

    @NonNull
    public static FilePropertiesPermissionsTabFragment newInstance() {
        //noinspection deprecation
        return new FilePropertiesPermissionsTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(Settings.MATERIAL_DESIGN_2.getValue() ?
                        R.layout.file_properties_permissions_tab_fragment_md2
                        : R.layout.file_properties_permissions_tab_fragment, container, false);
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

        mViewModel.getFileLiveData().observe(this, this::onFileChanged);
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
        BasicFileAttributes attributes = file.getAttributes();
        if (attributes instanceof PosixFileAttributes) {
            PosixFileAttributes posixAttributes = (PosixFileAttributes) attributes;
            PosixUser owner = posixAttributes.owner();
            String ownerString = owner != null ? owner.getName() != null ? getString(
                    R.string.file_properties_permissions_principal_format, owner.getName(),
                    owner.getId()) : String.valueOf(owner.getId()) : getString(R.string.unknown);
            mOwnerText.setText(ownerString);
            mOwnerText.setOnClickListener(view -> SetOwnerDialogFragment.show(file, this));
            PosixGroup group = posixAttributes.group();
            String groupString = group != null ? group.getName() != null ? getString(
                    R.string.file_properties_permissions_principal_format, group.getName(),
                    group.getId()) : String.valueOf(group.getId()) : getString(R.string.unknown);
            mGroupText.setText(groupString);
            mGroupText.setOnClickListener(view -> SetGroupDialogFragment.show(file, this));
            Set<PosixFileModeBit> mode = posixAttributes.mode();
            String modeString = mode != null ? getString(
                    R.string.file_properties_permissions_mode_format, PosixFileMode.toString(mode),
                    PosixFileMode.toInt(mode)) : getString(R.string.unknown);
            mModeText.setText(modeString);
            mModeText.setOnClickListener(view -> {
                if (!attributes.isSymbolicLink()) {
                    SetModeDialogFragment.show(file, this);
                }
            });
            String seLinuxContext = ObjectUtils.toStringOrNull(posixAttributes.seLinuxContext());
            ViewUtils.setVisibleOrGone(mSeLinuxContextLayout, seLinuxContext != null);
            mSeLinuxContextText.setText(seLinuxContext);
            mSeLinuxContextText.setOnClickListener(view -> SetSeLinuxContextDialogFragment.show(
                    file, this));
        }
        // TODO: Other attributes?
    }
}
