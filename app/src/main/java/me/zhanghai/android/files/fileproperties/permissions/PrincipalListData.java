/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java9.util.function.Predicate;
import me.zhanghai.android.files.util.StatefulData;
import me.zhanghai.java.functional.Functional;

public class PrincipalListData extends StatefulData<List<PrincipalItem>> {

    private PrincipalListData(@NonNull State state, @Nullable List<PrincipalItem> data,
                              @Nullable Exception exception) {
        super(state, data, exception);
    }

    @NonNull
    public static PrincipalListData ofLoading() {
        return new PrincipalListData(State.LOADING, null, null);
    }

    @NonNull
    public static PrincipalListData ofError(@NonNull Exception exception) {
        return new PrincipalListData(State.ERROR, null, exception);
    }

    @NonNull
    public static PrincipalListData ofSuccess(@NonNull List<PrincipalItem> userList) {
        return new PrincipalListData(State.SUCCESS, userList, null);
    }

    @NonNull
    public PrincipalListData filter(@NonNull Predicate<PrincipalItem> predicate) {
        if (data == null) {
            return this;
        }
        return new PrincipalListData(state, Functional.filter(data, predicate), exception);
    }
}
