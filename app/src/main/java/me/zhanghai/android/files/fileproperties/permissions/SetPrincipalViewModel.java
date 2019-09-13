/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.text.TextUtils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.zhanghai.android.files.util.SelectionLiveData;
import me.zhanghai.java.functional.Functional;

public abstract class SetPrincipalViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<PrincipalListData> mPrincipalListLiveData =
            createPrincipalListLiveData();

    @NonNull
    private final MutableLiveData<String> mFilterLiveData = new MutableLiveData<>();

    @NonNull
    private final LiveData<PrincipalListData> mFilteredPrincipalListLiveData =
            new FilteredPrincipalListLiveData(mPrincipalListLiveData, mFilterLiveData);

    @NonNull
    private final SelectionLiveData<Integer> mSelectionLiveData = new SelectionLiveData<>();

    protected abstract MutableLiveData<PrincipalListData> createPrincipalListLiveData();

    public void setFilter(@NonNull String filter) {
        if (!Objects.equals(mFilterLiveData.getValue(), filter)) {
            mFilterLiveData.setValue(filter);
        }
    }

    @NonNull
    public PrincipalListData getPrincipalListData() {
        return mPrincipalListLiveData.getValue();
    }

    @NonNull
    public LiveData<PrincipalListData> getFilteredPrincipalListLiveData() {
        return mFilteredPrincipalListLiveData;
    }

    @NonNull
    public SelectionLiveData<Integer> getSelectionLiveData() {
        return mSelectionLiveData;
    }

    private static class FilteredPrincipalListLiveData extends MediatorLiveData<PrincipalListData> {

        @NonNull
        private LiveData<PrincipalListData> mPrincipalListLiveData;
        @NonNull
        private LiveData<String> mFilterLiveData;

        public FilteredPrincipalListLiveData(
                @NonNull LiveData<PrincipalListData> principalListLiveData,
                @NonNull LiveData<String> filterLiveData) {

            mPrincipalListLiveData = principalListLiveData;
            mFilterLiveData = filterLiveData;

            addSource(mPrincipalListLiveData, userList -> loadValue());
            addSource(mFilterLiveData, filter -> loadValue());
        }

        private void loadValue() {
            String filter = mFilterLiveData.getValue();
            PrincipalListData principalListData = mPrincipalListLiveData.getValue();
            if (!TextUtils.isEmpty(filter)) {
                principalListData = principalListData.filter(user -> filterPrincipal(user, filter));
            }
            setValue(principalListData);
        }

        private static boolean filterPrincipal(@NonNull PrincipalItem principal,
                                               @NonNull String filter) {
            return Integer.toString(principal.id).contains(filter)
                    || (principal.name != null && principal.name.contains(filter))
                    || (Functional.some(principal.applicationInfos, applicationInfo ->
                    applicationInfo.packageName.contains(filter)))
                    || (Functional.some(principal.applicationLabels, applicationLabel ->
                    applicationLabel.contains(filter)));
        }
    }
}
