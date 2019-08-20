/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.takisoft.preferencex.EditTextPreference;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class DefaultIfEmptyEditTextPreference extends EditTextPreference {

    private String mDefaultValue;

    public DefaultIfEmptyEditTextPreference(@NonNull Context context) {
        super(context);
    }

    public DefaultIfEmptyEditTextPreference(@NonNull Context context,
                                            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultIfEmptyEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultIfEmptyEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        mDefaultValue = (String) super.onGetDefaultValue(a, index);
        return mDefaultValue;
    }

    @Override
    public void setDefaultValue(@Nullable Object defaultValue) {
        super.setDefaultValue(defaultValue);
        mDefaultValue = (String) defaultValue;
    }

    @Override
    public void setText(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            text = mDefaultValue;
        }
        super.setText(text);
    }
}
