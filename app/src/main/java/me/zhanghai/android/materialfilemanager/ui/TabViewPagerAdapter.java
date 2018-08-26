/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class TabViewPagerAdapter extends PagerAdapter {

    private static final String KEY_PREFIX = TabViewPagerAdapter.class.getName() + '.';

    private static final String STATE_HIERARCHY_STATE_FORMAT = KEY_PREFIX + "hierarchy_state_%1$d";

    private View[] mViews;
    private CharSequence[] mTitles;

    public TabViewPagerAdapter(View[] views, CharSequence[] titles) {
        init(views, titles);
    }

    public TabViewPagerAdapter(View[] views, int[] titleResIds, Context context) {

        CharSequence[] titles = new CharSequence[titleResIds.length];
        for (int i = 0; i < titleResIds.length; ++i) {
            titles[i] = context.getText(titleResIds[i]);
        }

        init(views, titles);
    }

    private void init(View[] views, CharSequence[] titles) {

        if (views.length != titles.length) {
            throw new IllegalArgumentException("View size and title size mismatch");
        }

        mViews = views;
        mTitles = titles;
    }

    @Override
    public int getCount() {
        return mViews.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = mViews[position];
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeViewAt(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Parcelable saveState() {
        Bundle state = new Bundle();
        for (int i = 0; i < mViews.length; ++i) {
            SparseArray<Parcelable> hierarchyState = new SparseArray<>();
            mViews[i].saveHierarchyState(hierarchyState);
            state.putSparseParcelableArray(makeHierarchyStateKey(i), hierarchyState);
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state == null) {
            return;
        }
        Bundle stateBundle = (Bundle) state;
        stateBundle.setClassLoader(loader);
        for (int i = 0; i < mViews.length; ++i) {
            SparseArray<Parcelable> hierarchyState = stateBundle.getSparseParcelableArray(
                    makeHierarchyStateKey(i));
            if (hierarchyState == null) {
                continue;
            }
            mViews[i].restoreHierarchyState(hierarchyState);
        }
    }

    private String makeHierarchyStateKey(int position) {
        return String.format(STATE_HIERARCHY_STATE_FORMAT, position);
    }
}
