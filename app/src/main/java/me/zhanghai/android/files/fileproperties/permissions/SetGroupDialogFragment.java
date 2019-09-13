/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixPrincipal;
import me.zhanghai.android.files.util.SelectionLiveData;

public class SetGroupDialogFragment extends SetPrincipalDialogFragment {

    @NonNull
    public static SetGroupDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        SetGroupDialogFragment fragment = new SetGroupDialogFragment();
        fragment.putArguments(file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        SetGroupDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public SetGroupDialogFragment() {}

    @StringRes
    protected int getTitleRes() {
        return R.string.file_properties_permissions_set_group_title;
    }

    @NonNull
    @Override
    protected Class<? extends SetPrincipalViewModel> getViewModelClass() {
        return SetGroupViewModel.class;
    }

    @NonNull
    @Override
    protected PrincipalListAdapter createAdapter(
            @NonNull SelectionLiveData<Integer> selectionLiveData) {
        return new GroupListAdapter(this, selectionLiveData);
    }

    @NonNull
    @Override
    protected PosixPrincipal getExtraPrincipal(@NonNull PosixFileAttributes attributes) {
        return attributes.group();
    }

    @Override
    protected void setPrincipal(@NonNull Path path, @NonNull PrincipalItem principal,
                                boolean recursive) {
        PosixGroup group = new PosixGroup(principal.id, ByteString.fromStringOrNull(
                principal.name));
        FileJobService.setGroup(path, group, recursive, requireContext());
    }
}
