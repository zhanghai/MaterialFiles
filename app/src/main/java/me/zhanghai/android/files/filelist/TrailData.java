/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.filesystem.File;

public class TrailData {

    @NonNull
    private final List<File> mTrail;
    @NonNull
    private final List<Parcelable> mStates;
    private final int mCurrentIndex;

    private TrailData(@NonNull List<File> trail, @NonNull List<Parcelable> states,
                      int currentIndex) {
        mTrail = trail;
        mStates = states;
        mCurrentIndex = currentIndex;
    }

    @NonNull
    public static TrailData of(@NonNull File file) {
        List<File> trail = file.makeTrail();
        List<Parcelable> states = new ArrayList<>(Collections.nCopies(trail.size(), null));
        int index = trail.size() - 1;
        return new TrailData(trail, states, index);
    }

    @NonNull
    public TrailData navigateTo(@NonNull Parcelable lastState, @NonNull File file) {
        List<File> newTrail = file.makeTrail();
        List<Parcelable> newStates = new ArrayList<>();
        int newIndex = newTrail.size() - 1;
        boolean isPrefix = true;
        for (int i = 0; i < newTrail.size(); ++i) {
            if (isPrefix && i < mTrail.size()) {
                if (Objects.equals(newTrail.get(i), mTrail.get(i))) {
                    newStates.add(i != mCurrentIndex ? mStates.get(i) : lastState);
                } else {
                    isPrefix = false;
                    newStates.add(null);
                }
            } else {
                newStates.add(null);
            }
        }
        if (isPrefix) {
            for (int i = newTrail.size(); i < mTrail.size(); ++i) {
                newTrail.add(mTrail.get(i));
                newStates.add(i != mCurrentIndex ? mStates.get(i) : lastState);
            }
        }
        return new TrailData(newTrail, newStates, newIndex);
    }

    @Nullable
    public TrailData navigateUp() {
        if (mCurrentIndex == 0) {
            return null;
        }
        int newIndex = mCurrentIndex - 1;
        return new TrailData(mTrail, mStates, newIndex);
    }

    @NonNull
    public List<File> getTrail() {
        return mTrail;
    }

    @Nullable
    public Parcelable getPendingState() {
        return mStates.set(mCurrentIndex, null);
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @NonNull
    public File getCurrentFile() {
        return mTrail.get(mCurrentIndex);
    }
}
