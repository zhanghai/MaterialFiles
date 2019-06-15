/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

public abstract class ViewStatePagerAdapter extends PagerAdapter {

    private static final String KEY_PREFIX = ViewStatePagerAdapter.class.getName() + '.';

    private static final String STATE_VIEW_STATES_SIZE = KEY_PREFIX + "VIEW_STATES_SIZE";
    private static final String STATE_VIEW_STATE_PREFIX = KEY_PREFIX + "VIEW_STATE_";

    @NonNull
    private final SparseArray<View> mViews = new SparseArray<>();
    @NonNull
    private final SparseArray<SparseArray<Parcelable>> mViewStates = new SparseArray<>();

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public final View instantiateItem(@NonNull ViewGroup container, int position) {
        View view = onCreateView(container, position);
        restoreViewState(position, view);
        mViews.put(position, view);
        return view;
    }

    @NonNull
    protected abstract View onCreateView(@NonNull ViewGroup container, int position);

    @Override
    public final void destroyItem(@NonNull ViewGroup container, int position,
                                  @NonNull Object object) {
        View view = (View) object;
        saveViewState(position, view);
        onDestroyView(container, position, view);
        mViews.remove(position);
        container.removeView(view);
    }

    protected abstract void onDestroyView(@NonNull ViewGroup container, int position,
                                          @NonNull View view);

    @NonNull
    @Override
    public Parcelable saveState() {
        for (int i = 0, size = mViews.size(); i < size; ++i) {
            saveViewState(mViews.keyAt(i), mViews.valueAt(i));
        }
        Bundle bundle = new Bundle();
        int size = mViewStates.size();
        bundle.putInt(STATE_VIEW_STATES_SIZE, size);
        for (int i = 0; i < size; ++i) {
            bundle.putSparseParcelableArray(makeViewStateKey(mViewStates.keyAt(i)),
                    mViewStates.valueAt(i));
        }
        return bundle;
    }

    @Override
    public void restoreState(@NonNull Parcelable state, @Nullable ClassLoader loader) {
        Bundle bundle = (Bundle) state;
        bundle.setClassLoader(loader);
        int size = bundle.getInt(STATE_VIEW_STATES_SIZE);
        for (int i = 0; i < size; ++i) {
            mViewStates.put(i, bundle.getSparseParcelableArray(makeViewStateKey(i)));
        }
    }

    @NonNull
    private String makeViewStateKey(int position) {
        return STATE_VIEW_STATE_PREFIX + position;
    }

    private void saveViewState(int position, @NonNull View view) {
        SparseArray<Parcelable> viewState = new SparseArray<>();
        view.saveHierarchyState(viewState);
        mViewStates.put(position, viewState);
    }

    private void restoreViewState(int position, @NonNull View view) {
        SparseArray<Parcelable> viewState = mViewStates.get(position);
        if (viewState != null) {
            view.restoreHierarchyState(viewState);
        }
    }
}
