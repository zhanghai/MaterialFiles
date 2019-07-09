/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;
import me.zhanghai.android.files.R;

public abstract class BaseColorPreference extends DialogPreference {

    public BaseColorPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public BaseColorPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public BaseColorPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public BaseColorPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.color_preference_widget);
        setDialogLayoutResource(R.layout.color_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View swatchView = holder.findViewById(R.id.swatch);
        if (swatchView != null) {
            GradientDrawable swatchDrawable = (GradientDrawable) swatchView.getBackground();
            swatchDrawable.setColor(getValue());
        }
    }

    @ColorInt
    public abstract int getValue();

    public abstract void setValue(@ColorInt int value);

    @ColorInt
    public abstract int getDefaultValue();

    @NonNull
    public abstract int[] getEntryValues();
}
