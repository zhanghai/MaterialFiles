/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class PathHistory {

    private List<List<File>> mHistory = new ArrayList<>();
    private List<File> mTrail = new ArrayList<>();
    private int mTrailIndex;

    public void push(List<File> path) {
        CollectionUtils.push(mHistory, path);
        updateTrail(path);
    }

    public boolean pop() {
        if (mHistory.size() < 2) {
            return false;
        }
        CollectionUtils.pop(mHistory);
        List<File> path = CollectionUtils.peek(mHistory);
        updateTrail(path);
        return true;
    }

    private void updateTrail(List<File> path) {
        if (!CollectionUtils.isPrefix(path, mTrail)) {
            mTrail.clear();
            mTrail.addAll(path);
        }
        mTrailIndex = path.size() - 1;
    }

    public List<File> getTrail() {
        return mTrail;
    }

    public int getTrailIndex() {
        return mTrailIndex;
    }

    public File getCurrentFile() {
        return mTrail.get(mTrailIndex);
    }
}
