/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import me.zhanghai.android.files.util.StatefulData

class PrincipalListData private constructor(
    state: State,
    data: List<PrincipalItem>?,
    exception: Exception?
) : StatefulData<List<PrincipalItem>>(state, data, exception) {
    fun filter(predicate: (PrincipalItem) -> Boolean): PrincipalListData =
        if (data == null) {
            this
        } else {
            PrincipalListData(state, data.filter(predicate), exception)
        }

    companion object {
        fun ofLoading(): PrincipalListData = PrincipalListData(State.LOADING, null, null)

        fun ofError(exception: Exception): PrincipalListData =
            PrincipalListData(State.ERROR, null, exception)

        fun ofSuccess(userList: List<PrincipalItem>): PrincipalListData =
            PrincipalListData(State.SUCCESS, userList, null)
    }
}
