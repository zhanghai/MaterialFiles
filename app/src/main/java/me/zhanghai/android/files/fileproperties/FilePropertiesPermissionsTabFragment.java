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
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.linux.LinuxFileAttributes;
import me.zhanghai.android.files.util.FragmentUtils;

public class FilePropertiesPermissionsTabFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = FilePropertiesPermissionsTabFragment.class.getName()
            + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.owner)
    Button mOwnerButton;
    @BindView(R.id.group)
    Button mGroupButton;
    @BindView(R.id.mode)
    Button mModeButton;
    @BindView(R.id.selinux_context)
    Button mSeLinuxContextButton;

    @NonNull
    private FileItem mExtraFile;

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public FilePropertiesPermissionsTabFragment() {}

    @NonNull
    public static FilePropertiesPermissionsTabFragment newInstance(@NonNull FileItem file) {
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

        BasicFileAttributes attributes = mExtraFile.getAttributes();
        if (attributes instanceof LinuxFileAttributes) {
            LinuxFileAttributes linuxAttributes = (LinuxFileAttributes) attributes;
            PosixUser owner = linuxAttributes.owner();
            String ownerString = owner.getName() != null ? getString(
                    R.string.file_properties_permissions_owner_format, owner.getName(),
                    owner.getId()) : String.valueOf(owner.getId());
            mOwnerButton.setText(ownerString);
            PosixGroup group = linuxAttributes.group();
            String groupString = group.getName() != null ? getString(
                    R.string.file_properties_permissions_group_format, group.getName(),
                    group.getId()) : String.valueOf(group.getId());
            mGroupButton.setText(groupString);
            Set<PosixFileModeBit> mode = linuxAttributes.mode();
            String modeString = getString(R.string.file_properties_permissions_mode_format,
                    PosixFileMode.toString(mode), PosixFileMode.toInt(mode));
            mModeButton.setText(modeString);
            String seLinuxContext = linuxAttributes.seLinuxContext();
            mSeLinuxContextButton.setText(seLinuxContext);
        }
        // TODO: ArchiveFileAttributes
    }
}
