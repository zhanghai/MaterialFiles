/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SaveStateSubsamplingScaleImageView extends SubsamplingScaleImageView {

    private ImageViewState mPendingSavedState;

    public SaveStateSubsamplingScaleImageView(@NonNull Context context) {
        super(context);
    }

    public SaveStateSubsamplingScaleImageView(@NonNull Context context,
                                              @Nullable AttributeSet attr) {
        super(context, attr);
    }

    public void setImageRestoringSavedState(@NonNull ImageSource imageSource) {
        setImage(imageSource, mPendingSavedState);
        mPendingSavedState = null;
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.state = getState();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mPendingSavedState = savedState.state;
    }

    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public ImageViewState state;

        public SavedState(Parcel in) {
            super(in);

            state = (ImageViewState) in.readSerializable();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeSerializable(state);
        }
    }
}
