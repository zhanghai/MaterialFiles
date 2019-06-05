/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import java8.nio.file.Path;
import java9.util.function.Function;
import me.zhanghai.android.files.navigation.NavigationRoot;
import me.zhanghai.android.files.navigation.NavigationRootMapLiveData;

public class BreadcrumbLiveData extends MediatorLiveData<BreadcrumbData> {

    @NonNull
    private LiveData<TrailData> mTrailLiveData;
    @NonNull
    private NavigationRootMapLiveData mNavigationRootMapLiveData =
            NavigationRootMapLiveData.getInstance();

    public BreadcrumbLiveData(@NonNull LiveData<TrailData> trailLiveData) {

        mTrailLiveData = trailLiveData;

        addSource(mTrailLiveData, file -> loadValue());
        addSource(mNavigationRootMapLiveData, fileNavigationRootMap -> loadValue());
    }

    private void loadValue() {
        Map<Path, NavigationRoot> navigationRootMap = mNavigationRootMapLiveData.getValue();
        TrailData trailData = mTrailLiveData.getValue();
        List<Path> trail = trailData.getTrail();
        List<Path> paths = new ArrayList<>();
        List<Function<Context, String>> names = new ArrayList<>();
        int selectedIndex = trailData.getCurrentIndex();
        for (Path path : trail) {
            NavigationRoot navigationRoot = navigationRootMap.get(path);
            int itemCount = names.size();
            if (navigationRoot != null && selectedIndex >= itemCount) {
                selectedIndex -= itemCount;
                paths.clear();
                paths.add(navigationRoot.getPath());
                names.clear();
                names.add(navigationRoot::getName);
            } else {
                paths.add(path);
                names.add(context -> FileUtils.getName(path));
            }
        }
        BreadcrumbData breadcrumbData = new BreadcrumbData(paths, names, selectedIndex);
        setValue(breadcrumbData);
    }
}
