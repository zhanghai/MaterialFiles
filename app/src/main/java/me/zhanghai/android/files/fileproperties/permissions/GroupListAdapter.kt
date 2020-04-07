/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.SelectionLiveData

class GroupListAdapter(
    fragment: Fragment,
    selectionLiveData: SelectionLiveData<Int>
) : PrincipalListAdapter(fragment, selectionLiveData) {
    @DrawableRes
    override val principalIconRes: Int = R.drawable.people_icon_control_normal_24dp
}
