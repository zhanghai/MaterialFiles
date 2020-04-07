/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.zhanghai.android.files.util.SelectionLiveData
import me.zhanghai.android.files.util.valueCompat

abstract class SetPrincipalViewModel(
    private val principalListLiveData: MutableLiveData<PrincipalListData>
) : ViewModel() {
    val principalListData: PrincipalListData
        get() = principalListLiveData.valueCompat

    private val filterLiveData = MutableLiveData("")
    var filter: String
        get() = filterLiveData.valueCompat
        set(value) {
            if (filterLiveData.valueCompat != value) {
                filterLiveData.value = value
            }
        }

    val filteredPrincipalListLiveData: LiveData<PrincipalListData> =
        FilteredPrincipalListLiveData(principalListLiveData, filterLiveData)

    val selectionLiveData = SelectionLiveData<Int>()

    private class FilteredPrincipalListLiveData(
        private val principalListLiveData: LiveData<PrincipalListData>,
        private val filterLiveData: LiveData<String>
    ) : MediatorLiveData<PrincipalListData>() {
        init {
            addSource(principalListLiveData) { loadValue() }
            addSource(filterLiveData) { loadValue() }
        }

        private fun loadValue() {
            val filter = filterLiveData.valueCompat
            var principalListData = principalListLiveData.valueCompat
            if (filter.isNotEmpty()) {
                principalListData = principalListData.filter { filterPrincipal(it, filter) }
            }
            value = principalListData
        }

        private fun filterPrincipal(principal: PrincipalItem, filter: String): Boolean =
            (filter in principal.id.toString()
                || (principal.name != null && filter in principal.name)
                || principal.applicationInfos.any { filter in it.packageName }
                || principal.applicationLabels.any { filter in it })
    }
}
