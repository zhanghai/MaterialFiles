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

public class ChangeOwnerViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<UserListData> mUserListLiveData = new UserListLiveData();

    @NonNull
    private final MutableLiveData<String> mFilterLiveData = new MutableLiveData<>();

    @NonNull
    private final LiveData<UserListData> mFilteredUserListLiveData = new FilteredUserListLiveData(
            mUserListLiveData, mFilterLiveData);

    @NonNull
    private final SelectionLiveData<Integer> mSelectionLiveData = new SelectionLiveData<>();

    public void setFilter(@NonNull String filter) {
        if (!Objects.equals(mFilterLiveData.getValue(), filter)) {
            mFilterLiveData.setValue(filter);
        }
    }

    @NonNull
    public LiveData<UserListData> getFilteredUserListLiveData() {
        return mFilteredUserListLiveData;
    }

    @NonNull
    public SelectionLiveData<Integer> getSelectionLiveData() {
        return mSelectionLiveData;
    }

    private static class FilteredUserListLiveData extends MediatorLiveData<UserListData> {

        @NonNull
        private LiveData<UserListData> mUserListLiveData;
        @NonNull
        private LiveData<String> mFilterLiveData;

        public FilteredUserListLiveData(@NonNull LiveData<UserListData> userListLiveData,
                                        @NonNull LiveData<String> filterLiveData) {

            mUserListLiveData = userListLiveData;
            mFilterLiveData = filterLiveData;

            addSource(mUserListLiveData, userList -> loadValue());
            addSource(mFilterLiveData, filter -> loadValue());
        }

        private void loadValue() {
            String filter = mFilterLiveData.getValue();
            UserListData userListData = mUserListLiveData.getValue();
            if (!TextUtils.isEmpty(filter)) {
                userListData = userListData.filter(user -> filterUser(user, filter));
            }
            setValue(userListData);
        }

        private static boolean filterUser(@NonNull UserItem user, @NonNull String filter) {
            return Integer.toString(user.uid).contains(filter)
                    || (user.name != null && user.name.contains(filter))
                    || (Functional.some(user.applicationInfos, applicationInfo ->
                    applicationInfo.packageName.contains(filter)))
                    || (Functional.some(user.applicationLabels, applicationLabel ->
                    applicationLabel.contains(filter)));
        }
    }
}
