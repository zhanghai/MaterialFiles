/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.Files;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class PathHistory {

    private List<List<File>> mPathHistory = new ArrayList<>();
    private List<Parcelable> mStateHistory = new ArrayList<>();
    private List<File> mTrail = new ArrayList<>();
    private int mTrailIndex;

    public void push(Parcelable lastState, List<File> path) {
        if (!mPathHistory.isEmpty()) {
            CollectionUtils.push(mStateHistory, lastState);
        }
        CollectionUtils.push(mPathHistory, path);
        updateTrail(path);
    }

    public boolean pop() {
        if (mPathHistory.size() < 2) {
            return false;
        }
        CollectionUtils.pop(mPathHistory);
        List<File> path = CollectionUtils.peek(mPathHistory);
        updateTrail(path);
        return true;
    }

    public Parcelable getPendingState() {
        if (mStateHistory.size() != mPathHistory.size()) {
            return null;
        }
        return CollectionUtils.pop(mStateHistory);
    }

    private void updateTrail(List<File> path) {
        if (CollectionUtils.isPrefix(Functional.map(path, File::getPath),
                        Functional.map(mTrail, File::getPath))) {
            Functional.forEach(path, (file, index) -> mTrail.set(index, file));
        } else {
            mTrail.clear();
            mTrail.addAll(path);
        }
        mTrailIndex = path.size() - 1;
        Files.onTrailChanged(Collections.unmodifiableList(mTrail));
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
