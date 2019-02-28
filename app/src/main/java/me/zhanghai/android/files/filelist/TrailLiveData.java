/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import java8.nio.file.Path;

public class TrailLiveData extends LiveData<TrailData> {

    public void navigateTo(@NonNull Parcelable lastState, @NonNull Path path) {
        TrailData oldTrailData = getValue();
        if (oldTrailData == null) {
            resetTo(path);
            return;
        }
        setValue(oldTrailData.navigateTo(lastState, path));
    }

    public void resetTo(@NonNull Path path) {
        setValue(TrailData.of(path));
    }

    public boolean navigateUp() {
        TrailData oldTrailData = getValue();
        if (oldTrailData == null) {
            return false;
        }
        TrailData trailData = oldTrailData.navigateUp();
        if (trailData == null) {
            return false;
        }
        setValue(trailData);
        return true;
    }

    public void reload() {
        setValue(getValue());
    }
}
