/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.widget.EditText;

import com.takisoft.preferencex.EditTextPreference;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class NonNegativeIntegerPreference extends EditTextPreference {

    private int mInteger;

    private boolean mIntegerSet;

    public NonNegativeIntegerPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public NonNegativeIntegerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NonNegativeIntegerPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                        @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public NonNegativeIntegerPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                        @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setOnBindEditTextListener(this::onBindEditText);
    }

    private void onBindEditText(@NonNull EditText editText) {
        DigitsKeyListener keyListener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyListener = DigitsKeyListener.getInstance(null, false, false);
        } else {
            keyListener = DigitsKeyListener.getInstance(false, false);
        }
        editText.setKeyListener(keyListener);
    }

    @NonNull
    @Override
    protected Integer onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        int defaultValueInt = defaultValue != null ? (int) defaultValue : 0;
        setInteger(getPersistedInt(defaultValueInt));
    }

    @Override
    public void setText(@Nullable String text) {
        if (text == null) {
            return;
        }
        int integer;
        try {
            integer = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return;
        }
        setInteger(integer);
    }

    public void setInteger(int integer) {
        if (integer < 0) {
            return;
        }
        boolean changed = mInteger != integer;
        if (changed || !mIntegerSet) {
            mInteger = integer;
            mIntegerSet = true;
            persistInt(mInteger);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @NonNull
    @Override
    public String getText() {
        return Integer.toString(getInteger());
    }

    public int getInteger() {
        return mInteger;
    }

    @Override
    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.integer = getInteger();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setInteger(myState.integer);
    }

    private static class SavedState extends BaseSavedState {

        public int integer;

        public SavedState(@NonNull Parcelable superState) {
            super(superState);
        }


        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public SavedState(Parcel source) {
            super(source);

            integer = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(integer);
        }
    }
}
