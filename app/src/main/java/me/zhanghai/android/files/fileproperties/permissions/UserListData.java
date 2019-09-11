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

public class UserListData extends StatefulData<List<UserItem>> {

    private UserListData(@NonNull State state, @Nullable List<UserItem> data,
                         @Nullable Exception exception) {
        super(state, data, exception);
    }

    @NonNull
    public static UserListData ofLoading() {
        return new UserListData(State.LOADING, null, null);
    }

    @NonNull
    public static UserListData ofError(@NonNull Exception exception) {
        return new UserListData(State.ERROR, null, exception);
    }

    @NonNull
    public static UserListData ofSuccess(@NonNull List<UserItem> userList) {
        return new UserListData(State.SUCCESS, userList, null);
    }

    @NonNull
    public UserListData filter(@NonNull Predicate<UserItem> predicate) {
        if (data == null) {
            return this;
        }
        return new UserListData(state, Functional.filter(data, predicate), exception);
    }
}
