/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.graphics.Outline;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.Label;

import me.zhanghai.android.materialfilemanager.R;

public class FloatingActionMenu extends com.github.clans.fab.FloatingActionMenu {

    private FloatingActionButton mMenuFab;
    private ImageView mMenuIcon;

    public FloatingActionMenu(Context context) {
        super(context);

        init();
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setClipToPadding(false);
        setClosedOnTouchOutside(true);
        mMenuFab = (FloatingActionButton) getChildAt(0);
        mMenuIcon = (ImageView) getChildAt(1);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        float elevation = getResources().getDimension(R.dimen.design_fab_elevation);
        setElevation(elevation);
        float labelCornerRadius = getResources().getDimension(R.dimen.abc_control_corner_material);
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View view = getChildAt(i);
            if (view == mMenuFab) {
                mMenuFab.setElevation(elevation);
            } else if (view == mMenuIcon) {
                mMenuIcon.setElevation(elevation);
            } else if (view instanceof FloatingActionButton) {
                FloatingActionButton fab = (FloatingActionButton) view;
                fab.setElevation(elevation);
            } else {
                Label label = (Label) view;
                label.setElevation(elevation);
                label.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                                labelCornerRadius);
                    }
                });
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && isOpened()) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.opened = isOpened();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.opened) {
            open(false);
        }
    }

    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public boolean opened;

        public SavedState(Parcel in) {
            super(in);

            opened = in.readByte() != 0;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeByte(opened ? (byte) 1 : (byte) 0);
        }
    }
}
