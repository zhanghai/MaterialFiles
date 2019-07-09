/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceDialogFragmentCompat;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ArrayUtils;

public class ColorPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String KEY_PREFIX = ColorPreferenceDialogFragment.class.getName()
            + '.';

    private static final String STATE_COLORS = KEY_PREFIX + "COLORS";
    private static final String STATE_CHECKED_COLOR = KEY_PREFIX + "CHECKED_COLOR";
    private static final String STATE_DEFAULT_COLOR = KEY_PREFIX + "DEFAULT_COLOR";

    private int[] mColors;
    private int mCheckedColor;
    private int mDefaultColor;

    private GridView mPaletteGrid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            BaseColorPreference preference = getPreference();
            mColors = preference.getEntryValues();
            mCheckedColor = preference.getValue();
            mDefaultColor = preference.getDefaultValue();
        } else {
            mColors = savedInstanceState.getIntArray(STATE_COLORS);
            mCheckedColor = savedInstanceState.getInt(STATE_CHECKED_COLOR);
            mDefaultColor = savedInstanceState.getInt(STATE_DEFAULT_COLOR);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray(STATE_COLORS, mColors);
        int checkedPosition = mPaletteGrid.getCheckedItemPosition();
        int checkedColor = checkedPosition != -1 ? mColors[checkedPosition] : mCheckedColor;
        outState.putInt(STATE_CHECKED_COLOR, checkedColor);
        outState.putInt(STATE_DEFAULT_COLOR, mDefaultColor);
    }

    @Nullable
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        int theme = getTheme();
        Context themedContext = theme != 0 ? new ContextThemeWrapper(context, theme) : context;
        return super.onCreateDialogView(themedContext);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mPaletteGrid = ViewCompat.requireViewById(view, R.id.palette);
        mPaletteGrid.setAdapter(new ColorPaletteAdapter(mColors));
        int checkedPosition = ArrayUtils.indexOf(mColors, mCheckedColor);
        if (checkedPosition != -1) {
            mPaletteGrid.setItemChecked(checkedPosition, true);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        int checkedPosition = ArrayUtils.indexOf(mColors, mDefaultColor);
        if (checkedPosition != -1) {
            builder.setNeutralButton(R.string.default_, null);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
        int checkedPosition = ArrayUtils.indexOf(mColors, mDefaultColor);
        if (checkedPosition != -1) {
            // Override the listener here so that we won't close the dialog.
            dialog.setOnShowListener(dialog2 -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    .setOnClickListener(view -> mPaletteGrid.setItemChecked(checkedPosition,
                            true)));
        }
        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        int checkedPosition = mPaletteGrid.getCheckedItemPosition();
        if (checkedPosition == -1) {
            return;
        }
        int checkedColor = mColors[checkedPosition];
        getPreference().setValue(checkedColor);
    }

    @NonNull
    @Override
    public BaseColorPreference getPreference() {
        return (BaseColorPreference) super.getPreference();
    }
}
