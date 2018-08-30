/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.fileproperties;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.IndeterminateSwitch;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.LocalFile;
import me.zhanghai.android.materialfilemanager.filesystem.PosixFileModeBit;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.ObjectUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FilePropertiesPermissionsTabFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesPermissionsTabFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.owner)
    Button mOwnerButton;
    @BindView(R.id.owner_access)
    Button mOwnerAccessButton;
    @BindView(R.id.group)
    Button mGroupButton;
    @BindView(R.id.group_access)
    Button mGroupAccessButton;
    @BindView(R.id.others_access)
    Button mOthersAccessButton;
    @BindView(R.id.executable_layout)
    ViewGroup mExecutableLayout;
    @BindView(R.id.executable_switch_layout)
    ViewGroup mExecutableSwitchLayout;
    @BindView(R.id.executable)
    IndeterminateSwitch mExecutableSwitch;

    private File mFile;

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public FilePropertiesPermissionsTabFragment() {}

    public static FilePropertiesPermissionsTabFragment newInstance(File file) {
        //noinspection deprecation
        FilePropertiesPermissionsTabFragment fragment = new FilePropertiesPermissionsTabFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_properties_permissions_tab_fragment, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mFile instanceof LocalFile) {
            LocalFile file = (LocalFile) mFile;
            mOwnerButton.setText(ObjectUtils.firstNonNull(file.getOwner().name, String.valueOf(
                    file.getOwner().id)));
            boolean isDirectory = file.isDirectory();
            Set<PosixFileModeBit> mode = file.getMode();
            mOwnerAccessButton.setText(FilePropertiesPermissions.getOwnerPermissionsString(
                    isDirectory, mode, mOwnerAccessButton.getContext()));
            mGroupButton.setText(ObjectUtils.firstNonNull(file.getGroup().name, String.valueOf(
                    file.getGroup().id)));
            mGroupAccessButton.setText(FilePropertiesPermissions.getGroupPermissionsString(
                    isDirectory, mode, mGroupAccessButton.getContext()));
            mOthersAccessButton.setText(FilePropertiesPermissions.getOthersPermissionsString(
                    isDirectory, mode, mOthersAccessButton.getContext()));
            boolean showExecutable = !isDirectory;
            ViewUtils.setVisibleOrGone(mExecutableLayout, showExecutable);
            if (showExecutable) {
                mExecutableSwitchLayout.setOnClickListener(view -> mExecutableSwitch.toggle());
                Boolean executable = FilePropertiesPermissions.isExecutable(mode);
                if (executable != null) {
                    mExecutableSwitch.setChecked(executable);
                } else {
                    mExecutableSwitch.setIndeterminate(true);
                }
            }
        }
    }
}
