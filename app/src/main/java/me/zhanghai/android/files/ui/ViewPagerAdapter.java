/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public abstract class ViewPagerAdapter extends PagerAdapter {

    @NonNull
    @Override
    public final View instantiateItem(@NonNull ViewGroup container, int position) {
        return onCreateView(container, position);
    }

    @NonNull
    protected abstract View onCreateView(@NonNull ViewGroup container, int position);

    @Override
    public final void destroyItem(@NonNull ViewGroup container, int position,
                                  @NonNull Object object) {
        View view = (View) object;
        onDestroyView(container, position, view);
        container.removeView(view);
    }

    protected abstract void onDestroyView(@NonNull ViewGroup container, int position,
                                          @NonNull View view);

    @Override
    public final boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public final int getItemPosition(@NonNull Object object) {
        View view = (View) object;
        return getViewPosition(view);
    }

    protected int getViewPosition(@NonNull View view) {
        return POSITION_UNCHANGED;
    }
}
