/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import androidx.lifecycle.MutableLiveData;

public class SetGroupViewModel extends SetPrincipalViewModel {

    @Override
    protected MutableLiveData<PrincipalListData> createPrincipalListLiveData() {
        return new GroupListLiveData();
    }
}
