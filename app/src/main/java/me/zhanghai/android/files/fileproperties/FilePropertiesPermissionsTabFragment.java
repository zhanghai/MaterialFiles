/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.os.Bundle;
import android.system.OsConstants;
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
import me.zhanghai.android.files.filesystem.PosixFileModeBit;
import me.zhanghai.android.files.filesystem.PosixGroup;
import me.zhanghai.android.files.filesystem.PosixUser;
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
                    getModeString(file.getMode()), getModeInt(mode));
            mModeButton.setText(modeString);
        }
    }

    @NonNull
    private static String getModeString(@NonNull Set<PosixFileModeBit> mode) {
        boolean hasSetUserIdBit = mode.contains(PosixFileModeBit.SET_USER_ID);
        boolean hasSetGroupIdBit = mode.contains(PosixFileModeBit.SET_GROUP_ID);
        boolean hasStickyBit = mode.contains(PosixFileModeBit.STICKY);
        return new StringBuilder()
                .append(mode.contains(PosixFileModeBit.OWNER_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.OWNER_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.OWNER_EXECUTE) ? hasSetUserIdBit ? 's' : 'x'
                        : hasSetUserIdBit ? 'S' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_EXECUTE) ? hasSetGroupIdBit ? 's' : 'x'
                        : hasSetGroupIdBit ? 'S' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_EXECUTE) ? hasStickyBit ? 't' : 'x'
                        : hasStickyBit ? 'T' : '-')
                .toString();
    }

    private static int getModeInt(@NonNull Set<PosixFileModeBit> mode) {
        return (mode.contains(PosixFileModeBit.SET_USER_ID) ? OsConstants.S_ISUID : 0)
                | (mode.contains(PosixFileModeBit.SET_GROUP_ID) ? OsConstants.S_ISGID : 0)
                | (mode.contains(PosixFileModeBit.STICKY) ? OsConstants.S_ISVTX : 0)
                | (mode.contains(PosixFileModeBit.OWNER_READ) ? OsConstants.S_IRUSR : 0)
                | (mode.contains(PosixFileModeBit.OWNER_WRITE) ? OsConstants.S_IWUSR : 0)
                | (mode.contains(PosixFileModeBit.OWNER_EXECUTE) ? OsConstants.S_IXUSR : 0)
                | (mode.contains(PosixFileModeBit.GROUP_READ) ? OsConstants.S_IRGRP : 0)
                | (mode.contains(PosixFileModeBit.GROUP_WRITE) ? OsConstants.S_IWGRP : 0)
                | (mode.contains(PosixFileModeBit.GROUP_EXECUTE) ? OsConstants.S_IXGRP : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_READ) ? OsConstants.S_IROTH : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_WRITE) ? OsConstants.S_IWOTH : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_EXECUTE) ? OsConstants.S_IXOTH : 0);
    }
}
