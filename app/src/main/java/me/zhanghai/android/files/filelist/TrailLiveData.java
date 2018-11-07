/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.filesystem.File;

public class TrailLiveData extends LiveData<TrailData> {

    public void navigateTo(@NonNull Parcelable lastState, @NonNull File file) {
        TrailData oldTrailData = getValue();
        if (oldTrailData == null) {
            resetTo(file);
            return;
        }
        setValue(oldTrailData.navigateTo(lastState, file));
    }

    public void resetTo(@NonNull File file) {
        setValue(TrailData.of(file));
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
