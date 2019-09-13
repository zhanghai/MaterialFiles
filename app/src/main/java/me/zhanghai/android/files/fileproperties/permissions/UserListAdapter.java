/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.SelectionLiveData;

class UserListAdapter extends PrincipalListAdapter {

    public UserListAdapter(@NonNull Fragment fragment,
                           @NonNull SelectionLiveData<Integer> selectionLiveData) {
        super(fragment, selectionLiveData);
    }

    @Override
    protected int getPrincipalIconRes() {
        return R.drawable.person_icon_control_normal_24dp;
    }
}
