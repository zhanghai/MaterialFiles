/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class Trail {

    private List<File> mTrail = new ArrayList<>();
    private List<Parcelable> mStates = new ArrayList<>();
    private int mIndex;

    public void navigateTo(Parcelable lastState, List<File> path) {
        if (mTrail.isEmpty()) {
            mTrail.addAll(path);
            mStates.addAll(Collections.nCopies(path.size(), null));
            mIndex = path.size() - 1;
            return;
        }
        mStates.set(mIndex, lastState);
        boolean isPrefix = Functional.every(path, (file, index) -> {
            if (index < mTrail.size()) {
                File oldFile = mTrail.get(index);
                mTrail.set(index, file);
                if (Objects.equals(oldFile.getUri(), file.getUri())) {
                    return true;
                } else {
                    mStates.set(index, null);
                    return false;
                }
            } else {
                mTrail.add(file);
                mStates.add(null);
                return true;
            }
        });
        if (mTrail.size() > path.size() && !isPrefix) {
            mTrail.subList(path.size(), mTrail.size()).clear();
            mStates.subList(path.size(), mStates.size()).clear();
        }
        mIndex = path.size() - 1;
        mStates.set(mIndex, null);
    }

    public boolean navigateUp() {
        if (mIndex < 1) {
            return false;
        }
        --mIndex;
        return true;
    }

    public Parcelable getPendingState() {
        Parcelable state = mStates.get(mIndex);
        mStates.set(mIndex, null);
        return state;
    }

    public List<File> getTrail() {
        return mTrail;
    }

    public int getIndex() {
        return mIndex;
    }

    public File getCurrentFile() {
        return mTrail.get(mIndex);
    }
}
