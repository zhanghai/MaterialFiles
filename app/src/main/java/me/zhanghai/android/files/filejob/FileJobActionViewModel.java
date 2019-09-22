/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

public class FileJobActionViewModel extends ViewModel {

    private final RemountStateLiveData mRemountStateLiveData = new RemountStateLiveData();

    @NonNull
    public RemountStateLiveData getRemountStateLiveData() {
        return mRemountStateLiveData;
    }
}
