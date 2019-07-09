/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kizitonwose.colorpreference.ColorDialog;
import com.kizitonwose.colorpreference.ColorShape;
import com.kizitonwose.colorpreference.ColorUtils;

import java.util.Objects;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ArrayUtils;

/*
 * @see https://github.com/kizitonwose/colorpreference/blob/master/support/src/main/java/com/kizitonwose/colorpreferencecompat/ColorPreferenceCompat.java
 */
public class CustomThemeColorPreference extends Preference
        implements ColorDialog.OnColorSelectedListener {

    private static final int NUM_COLUMNS = 5;
    private static final ColorShape COLOR_SHAPE = ColorShape.CIRCLE;

    private String mValue;
    private boolean mValueSet;

    private int[] mColors;

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

        setWidgetLayoutResource(R.layout.pref_color_layout);

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
        mColors = new int[colors.length];
        for (int i = 0; i < colors.length; ++i) {
            CustomThemeColor color = colors[i];
            mColors[i] = ContextCompat.getColor(context, color.getResourceId());
        }
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        boolean changed = !Objects.equals(mValue, value);
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
    }

    private int getColorFromValue() {
        return mColors[Integer.parseInt(getValue())];
    }

    private void setValueByColor(int color) {
        setValue(String.valueOf(ArrayUtils.indexOf(mColors, color)));
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ImageView colorView = (ImageView) holder.findViewById(R.id.color_view);
        if (colorView != null) {
            ColorUtils.setColorViewValue(colorView, getColorFromValue(), false, COLOR_SHAPE);
        }
    }

    @Override
    protected void onClick() {
        ColorUtils.showDialog(getContext(), this, getFragmentTag(), NUM_COLUMNS, COLOR_SHAPE,
                mColors, getColorFromValue());
    }

    @Override
    public void onAttached() {
        super.onAttached();

        ColorUtils.attach(getContext(), this, getFragmentTag());
    }

    @NonNull
    private String getFragmentTag() {
        return "color_" + getKey();
    }

    @Override
    public void onColorSelected(int color, String tag) {
        setValueByColor(color);
    }
}
