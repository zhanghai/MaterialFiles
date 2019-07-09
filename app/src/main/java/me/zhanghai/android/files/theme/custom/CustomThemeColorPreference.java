/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Objects;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.colorpicker.BaseColorPreference;
import me.zhanghai.android.files.colorpicker.ColorPreferenceDialogFragment;
import me.zhanghai.android.files.util.ArrayUtils;

public class CustomThemeColorPreference extends BaseColorPreference {

    static {
        PreferenceFragmentCompat.registerPreferenceFragment(CustomThemeColorPreference.class,
                ColorPreferenceDialogFragment.class);
    }

    private String mStringValue;
    private boolean mStringValueSet;

    private String mDefaultStringValue;

    private int[] mEntryValues;

    public CustomThemeColorPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public CustomThemeColorPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CustomThemeColorPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                      @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public CustomThemeColorPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                      @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        String key = getKey();
        Context context = getContext();
        CustomThemeColor[] colors;
        if (Objects.equals(key, context.getString(R.string.pref_key_primary_color))) {
            colors = CustomThemeColors.Primary.values();
        } else if (Objects.equals(key, context.getString(R.string.pref_key_accent_color))) {
            colors = CustomThemeColors.Accent.values();
        } else {
            throw new IllegalArgumentException("Unknown custom theme color preference key: " + key);
        }
        mEntryValues = new int[colors.length];
        for (int i = 0; i < colors.length; ++i) {
            CustomThemeColor color = colors[i];
            mEntryValues[i] = ContextCompat.getColor(context, color.getResourceId());
        }
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        mDefaultStringValue = a.getString(index);
        return mDefaultStringValue;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setStringValue(getPersistedString((String) defaultValue));
    }

    public String getStringValue() {
        return mStringValue;
    }

    public void setStringValue(String value) {
        boolean changed = !Objects.equals(mStringValue, value);
        if (changed || !mStringValueSet) {
            mStringValue = value;
            mStringValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @ColorInt
    @Override
    public int getValue() {
        return mEntryValues[Integer.parseInt(getStringValue())];
    }

    @Override
    public void setValue(@ColorInt int value) {
        setStringValue(String.valueOf(ArrayUtils.indexOf(mEntryValues, value)));
    }

    @ColorInt
    @Override
    public int getDefaultValue() {
        return mEntryValues[Integer.parseInt(mDefaultStringValue)];
    }

    @NonNull
    @Override
    public int[] getEntryValues() {
        return mEntryValues;
    }
}
