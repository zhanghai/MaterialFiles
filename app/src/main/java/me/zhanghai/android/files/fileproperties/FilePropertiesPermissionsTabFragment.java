/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.filesystem.LocalFile;
import me.zhanghai.android.files.filesystem.PosixGroup;
import me.zhanghai.android.files.filesystem.PosixUser;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.util.FragmentUtils;

public class FilePropertiesPermissionsTabFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesPermissionsTabFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.owner)
    Button mOwnerButton;
    @BindView(R.id.group)
    Button mGroupButton;
    @BindView(R.id.mode)
    Button mModeButton;

    @NonNull
    private File mExtraFile;

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public FilePropertiesPermissionsTabFragment() {}

    @NonNull
    public static FilePropertiesPermissionsTabFragment newInstance(@NonNull File file) {
        //noinspection deprecation
        FilePropertiesPermissionsTabFragment fragment = new FilePropertiesPermissionsTabFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_properties_permissions_tab_fragment, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mExtraFile instanceof LocalFile) {
            LocalFile file = (LocalFile) mExtraFile;
            PosixUser owner = file.getOwner();
            String ownerString = owner.name != null ? getString(
                    R.string.file_properties_permissions_owner_format, owner.name, owner.id) 
                    : String.valueOf(owner.id);
            mOwnerButton.setText(ownerString);
            PosixGroup group = file.getGroup();
            String groupString = group.name != null ? getString(
                    R.string.file_properties_permissions_group_format, group.name, group.id)
                    : String.valueOf(group.id);
            mGroupButton.setText(groupString);
            Set<PosixFileModeBit> mode = file.getMode();
            String modeString = getString(R.string.file_properties_permissions_mode_format,
                    PosixFileMode.toString(file.getMode()), PosixFileMode.toInt(mode));
            mModeButton.setText(modeString);
        }
    }
}
