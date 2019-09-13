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
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixPrincipal;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.util.SelectionLiveData;

public class SetOwnerDialogFragment extends SetPrincipalDialogFragment {

    @NonNull
    public static SetOwnerDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        SetOwnerDialogFragment fragment = new SetOwnerDialogFragment();
        fragment.putArguments(file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        SetOwnerDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public SetOwnerDialogFragment() {}

    @StringRes
    protected int getTitleRes() {
        return R.string.file_properties_permissions_set_owner_title;
    }

    @NonNull
    @Override
    protected Class<? extends SetPrincipalViewModel> getViewModelClass() {
        return SetOwnerViewModel.class;
    }

    @NonNull
    @Override
    protected PrincipalListAdapter createAdapter(
            @NonNull SelectionLiveData<Integer> selectionLiveData) {
        return new UserListAdapter(this, selectionLiveData);
    }

    @NonNull
    @Override
    protected PosixPrincipal getExtraPrincipal(@NonNull PosixFileAttributes attributes) {
        return attributes.owner();
    }

    @Override
    protected void setPrincipal(@NonNull Path path, @NonNull PrincipalItem principal,
                                boolean recursive) {
        PosixUser owner = new PosixUser(principal.id, ByteString.fromStringOrNull(principal.name));
        FileJobService.setOwner(path, owner, recursive, requireContext());
    }
}
