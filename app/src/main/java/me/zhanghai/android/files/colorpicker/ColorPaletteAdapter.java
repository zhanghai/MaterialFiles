/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorPaletteAdapter extends BaseAdapter {

    private int[] mColors;

    public ColorPaletteAdapter(int[] colors) {
        mColors = colors;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return mColors.length;
    }

    @Override
    public Integer getItem(int position) {
        return getItemColor(position);
    }

    public int getItemColor(int position) {
        return mColors[position];
    }

    @Override
    public long getItemId(int position) {
        return getItemColor(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ColorSwatchView swatchView = (ColorSwatchView) convertView;
        if (swatchView == null) {
            swatchView = new ColorSwatchView(parent.getContext());
            swatchView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        int color = getItemColor(position);
        swatchView.setColor(color);
        return swatchView;
    }
}
