/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
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
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;

public class TrailData {

    @NonNull
    private final List<Path> mTrail;
    @NonNull
    private final List<Parcelable> mStates;
    private final int mCurrentIndex;

    private TrailData(@NonNull List<Path> trail, @NonNull List<Parcelable> states,
                      int currentIndex) {
        mTrail = trail;
        mStates = states;
        mCurrentIndex = currentIndex;
    }

    @NonNull
    public static TrailData of(@NonNull Path path) {
        List<Path> trail = makeTrail(path);
        List<Parcelable> states = new ArrayList<>(Collections.nCopies(trail.size(), null));
        int index = trail.size() - 1;
        return new TrailData(trail, states, index);
    }

    @NonNull
    public TrailData navigateTo(@NonNull Parcelable lastState, @NonNull Path path) {
        List<Path> newTrail = makeTrail(path);
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
    public List<Path> getTrail() {
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
    public Path getCurrentPath() {
        return mTrail.get(mCurrentIndex);
    }

    @NonNull
    private static List<Path> makeTrail(@NonNull Path path) {
        List<Path> trail = new ArrayList<>();
        Path archiveFile = ArchiveFileSystemProvider.isArchivePath(path) ?
                ArchiveFileSystemProvider.getArchiveFile(path) : null;
        do {
            trail.add(path);
            path = path.getParent();
        } while (path != null);
        Collections.reverse(trail);
        if (archiveFile != null) {
            Path archiveFileParent = archiveFile.getParent();
            if (archiveFileParent != null) {
                trail.addAll(0, makeTrail(archiveFileParent));
            }
        }
        return trail;
    }
}
