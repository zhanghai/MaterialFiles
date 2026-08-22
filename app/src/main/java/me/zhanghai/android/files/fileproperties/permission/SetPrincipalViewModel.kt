/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.zhanghai.android.files.util.SelectionLiveData
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.valueCompat

abstract class SetPrincipalViewModel(
    private val principalListLiveData: PrincipalListLiveData
) : ViewModel() {
    val principalListStateful: Stateful<List<PrincipalItem>>
        get() = principalListLiveData.valueCompat

    private val filterLiveData = MutableLiveData("")
    var filter: String
        get() = filterLiveData.valueCompat
        set(value) {
            if (filterLiveData.valueCompat != value) {
                filterLiveData.value = value
            }
        }

    val filteredPrincipalListLiveData: LiveData<Stateful<List<PrincipalItem>>> =
        FilteredPrincipalListLiveData(principalListLiveData, filterLiveData)

    val selectionLiveData = SelectionLiveData<Int>()

    private class FilteredPrincipalListLiveData(
        private val principalListLiveData: PrincipalListLiveData,
        private val filterLiveData: LiveData<String>
    ) : MediatorLiveData<Stateful<List<PrincipalItem>>>() {
        init {
            addSource(principalListLiveData) { loadValue() }
            addSource(filterLiveData) { loadValue() }
        }

        private fun loadValue() {
            var principalListStateful = principalListLiveData.valueCompat
            val filter = filterLiveData.valueCompat
            if (principalListStateful is Success && filter.isNotEmpty()) {
                var filteredPrincipalList =
                    principalListStateful.value.filterTo(ArrayList()) { it.applyFilter(filter) }
                // Allow selecting an arbitrary id by typing it into the filter box, e.g. the uid
                // of an app from another Android user which we may not be able to enumerate.
                val filterId = filter.toIntOrNull()
                if (filterId != null && filterId >= 0
                    && filteredPrincipalList.none { it.id == filterId }) {
                    filteredPrincipalList.add(
                        0, PrincipalItem(
                            filterId,
                            principalListLiveData.getAppPrincipalName(filterId),
                            emptyList(),
                            emptyList()
                        )
                    )
                }
                principalListStateful = Success(filteredPrincipalList)
            }
            value = principalListStateful
        }

        private fun PrincipalItem.applyFilter(filter: String): Boolean =
            (filter in id.toString() || (name != null && filter in name)
                || applicationInfos.any { filter in it.packageName }
                || applicationLabels.any { filter in it })
    }
}
