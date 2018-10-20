/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.compat.Function;
import me.zhanghai.android.materialfilemanager.navigation.NavigationRoot;
import me.zhanghai.android.materialfilemanager.navigation.NavigationRootMapLiveData;

public class BreadcrumbLiveData extends MediatorLiveData<BreadcrumbData> {

    private LiveData<TrailData> mTrailLiveData;
    private NavigationRootMapLiveData mNavigationRootMapLiveData =
            NavigationRootMapLiveData.getInstance();

    public BreadcrumbLiveData(LiveData<TrailData> trailLiveData) {

        mTrailLiveData = trailLiveData;

        addSource(mTrailLiveData, file -> loadValue());
        addSource(mNavigationRootMapLiveData, fileNavigationRootMap -> loadValue());
    }

    private void loadValue() {
        Map<File, NavigationRoot> navigationRootMap = mNavigationRootMapLiveData.getValue();
        TrailData trailData = mTrailLiveData.getValue();
        List<File> trail = trailData.getTrail();
        List<File> files = new ArrayList<>();
        List<Function<Context, String>> names = new ArrayList<>();
        int selectedIndex = trailData.getCurrentIndex();
        for (File file : trail) {
            NavigationRoot navigationRoot = navigationRootMap.get(file);
            int itemCount = names.size();
            if (navigationRoot != null && selectedIndex >= itemCount) {
                selectedIndex -= itemCount;
                files.clear();
                files.add(navigationRoot.getFile());
                names.clear();
                names.add(navigationRoot::getName);
            } else {
                files.add(file);
                names.add(context -> file.getName());
            }
        }
        BreadcrumbData breadcrumbData = new BreadcrumbData(files, names, selectedIndex);
        setValue(breadcrumbData);
    }
}
