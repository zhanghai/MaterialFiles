/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foregroundcompat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.graphics.drawable.DrawableCompat;

public class ForegroundHelper {

    private static final int[] STYLEABLE = {
            android.R.attr.foreground,
            android.R.attr.foregroundGravity,
            android.R.attr.foregroundTintMode,
            android.R.attr.foregroundTint
    };
    private static final int STYLEABLE_ANDROID_FOREGROUND = 0;
    private static final int STYLEABLE_ANDROID_FOREGROUND_GRAVITY = 1;
    private static final int STYLEABLE_ANDROID_FOREGROUND_TINT_MODE = 2;
    private static final int STYLEABLE_ANDROID_FOREGROUND_TINT = 3;

    @NonNull
    private final View mView;

    private boolean mHasFrameworkImplementation;

    @Nullable
    private ForegroundInfo mForegroundInfo;

    public ForegroundHelper(@NonNull View view) {
        mView = view;
    }

    public void init(@NonNull Context context, @Nullable AttributeSet attrs,
                     @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {

        mHasFrameworkImplementation = mView instanceof FrameLayout
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M);

        if (mHasFrameworkImplementation) {
            return;
        }

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, STYLEABLE,
                defStyleAttr, defStyleRes);
        if (a.hasValue(STYLEABLE_ANDROID_FOREGROUND)) {
            setSupportForeground(a.getDrawable(STYLEABLE_ANDROID_FOREGROUND));
        }
        if (a.hasValue(STYLEABLE_ANDROID_FOREGROUND_GRAVITY)) {
            setSupportForegroundGravity(a.getInt(STYLEABLE_ANDROID_FOREGROUND_GRAVITY,
                    Gravity.NO_GRAVITY));
        }
        if (a.hasValue(STYLEABLE_ANDROID_FOREGROUND_TINT_MODE)) {
            setSupportForegroundTintMode(MoreDrawableCompat.parseTintMode(a.getInt(
                    STYLEABLE_ANDROID_FOREGROUND_TINT_MODE, -1), null));
        }
        if (a.hasValue(STYLEABLE_ANDROID_FOREGROUND_TINT)) {
            setSupportForegroundTintList(a.getColorStateList(STYLEABLE_ANDROID_FOREGROUND_TINT));
        }
        //if (a.hasValue(STYLEABLE_ANDROID_FOREGROUND_INSIDE_PADDING)) {
        //    if (mForegroundInfo == null) {
        //        mForegroundInfo = new ForegroundInfo();
        //    }
        //    mForegroundInfo.mInsidePadding = a.getBoolean(
        //            STYLEABLE_ANDROID_FOREGROUND_INSIDE_PADDING, mForegroundInfo.mInsidePadding);
        //}
        a.recycle();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void onVisibilityAggregated(boolean isVisible) {

        if (mHasFrameworkImplementation) {
            return;
        }

        Drawable fg = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (fg != null && isVisible != fg.isVisible()) {
            fg.setVisible(isVisible, false);
        }
    }

    public void draw(@NonNull Canvas canvas) {

        if (mHasFrameworkImplementation) {
            return;
        }

        onDrawForeground(canvas);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {

        if (mHasFrameworkImplementation) {
            return;
        }

        resolveForegroundDrawable(layoutDirection);
    }

    private void resolveForegroundDrawable(int layoutDirection) {
        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null) {
            DrawableCompat.setLayoutDirection(mForegroundInfo.mDrawable, layoutDirection);
        }
    }

    protected boolean verifyDrawable(@NonNull Drawable who) {

        if (mHasFrameworkImplementation) {
            return false;
        }

        return mForegroundInfo != null && mForegroundInfo.mDrawable == who;
    }

    public void drawableStateChanged() {

        if (mHasFrameworkImplementation) {
            return;
        }

        int[] state = mView.getDrawableState();
        boolean changed = false;
        Drawable fg = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        if (changed) {
            mView.invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {

        if (mHasFrameworkImplementation) {
            return;
        }

        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null) {
            mForegroundInfo.mDrawable.setHotspot(x, y);
        }
    }

    public void jumpDrawablesToCurrentState() {

        if (mHasFrameworkImplementation) {
            return;
        }

        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null) {
            mForegroundInfo.mDrawable.jumpToCurrentState();
        }
    }

    @Nullable
    public Drawable getSupportForeground() {
        return mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
    }

    public void setSupportForeground(@Nullable Drawable foreground) {

        if (mForegroundInfo == null) {
            if (foreground == null) {
                // Nothing to do.
                return;
            }
            mForegroundInfo = new ForegroundInfo();
        }

        if (foreground == mForegroundInfo.mDrawable) {
            // Nothing to do
            return;
        }

        if (mForegroundInfo.mDrawable != null) {
            if (mView.isAttachedToWindow()) {
                mForegroundInfo.mDrawable.setVisible(false, false);
            }
            mForegroundInfo.mDrawable.setCallback(null);
            mView.unscheduleDrawable(mForegroundInfo.mDrawable);
        }

        mForegroundInfo.mDrawable = foreground;
        //mForegroundInfo.mBoundsChanged = true;
        if (foreground != null) {
            DrawableCompat.setLayoutDirection(foreground, mView.getLayoutDirection());
            if (foreground.isStateful()) {
                foreground.setState(mView.getDrawableState());
            }
            applyForegroundTint();
            if (mView.isAttachedToWindow()) {
                foreground.setVisible(mView.getWindowVisibility() == View.VISIBLE
                        && mView.isShown(), false);
            }
            // Set callback last, since the view may still be initializing.
            foreground.setCallback(mView);
        }
        mView.requestLayout();
        mView.invalidate();
    }

    public int getSupportForegroundGravity() {
        return mForegroundInfo != null ? mForegroundInfo.mGravity : Gravity.START | Gravity.TOP;
    }

    public void setSupportForegroundGravity(int gravity) {

        if (mForegroundInfo == null) {
            mForegroundInfo = new ForegroundInfo();
        }

        if (mForegroundInfo.mGravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }

            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mForegroundInfo.mGravity = gravity;
            mView.requestLayout();
        }
    }

    public void setSupportForegroundTintList(@Nullable ColorStateList tint) {

        if (mForegroundInfo == null) {
            mForegroundInfo = new ForegroundInfo();
        }
        if (mForegroundInfo.mTintInfo == null) {
            mForegroundInfo.mTintInfo = new TintInfo();
        }
        mForegroundInfo.mTintInfo.mTintList = tint;
        mForegroundInfo.mTintInfo.mHasTintList = true;

        applyForegroundTint();
    }

    @Nullable
    public ColorStateList getSupportForegroundTintList() {
        return mForegroundInfo != null && mForegroundInfo.mTintInfo != null ?
                mForegroundInfo.mTintInfo.mTintList : null;
    }

    public void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {

        if (mForegroundInfo == null) {
            mForegroundInfo = new ForegroundInfo();
        }
        if (mForegroundInfo.mTintInfo == null) {
            mForegroundInfo.mTintInfo = new TintInfo();
        }
        mForegroundInfo.mTintInfo.mTintMode = tintMode;
        mForegroundInfo.mTintInfo.mHasTintMode = true;

        applyForegroundTint();
    }

    @Nullable
    public PorterDuff.Mode getSupportForegroundTintMode() {
        return mForegroundInfo != null && mForegroundInfo.mTintInfo != null
                ? mForegroundInfo.mTintInfo.mTintMode : null;
    }

    private void applyForegroundTint() {
        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null
                && mForegroundInfo.mTintInfo != null) {
            TintInfo tintInfo = mForegroundInfo.mTintInfo;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                mForegroundInfo.mDrawable = mForegroundInfo.mDrawable.mutate();

                if (tintInfo.mHasTintList) {
                    mForegroundInfo.mDrawable.setTintList(tintInfo.mTintList);
                }

                if (tintInfo.mHasTintMode) {
                    mForegroundInfo.mDrawable.setTintMode(tintInfo.mTintMode);
                }

                // The drawable (or one of its children) may not have been
                // stateful before applying the tint, so let's try again.
                if (mForegroundInfo.mDrawable.isStateful()) {
                    mForegroundInfo.mDrawable.setState(mView.getDrawableState());
                }
            }
        }
    }

    private void onDrawForeground(@NonNull Canvas canvas) {
        Drawable foreground = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (foreground != null) {
            //if (mForegroundInfo.mBoundsChanged) {
            //mForegroundInfo.mBoundsChanged = false;
            Rect selfBounds = mForegroundInfo.mSelfBounds;
            Rect overlayBounds = mForegroundInfo.mOverlayBounds;

            //if (mForegroundInfo.mInsidePadding) {
            selfBounds.set(0, 0, mView.getWidth(), mView.getHeight());
            //} else {
            //    selfBounds.set(getPaddingLeft(), getPaddingTop(),
            //            getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            //}

            int ld = mView.getLayoutDirection();
            Gravity.apply(mForegroundInfo.mGravity, foreground.getIntrinsicWidth(),
                    foreground.getIntrinsicHeight(), selfBounds, overlayBounds, ld);
            foreground.setBounds(overlayBounds);
            //}

            foreground.draw(canvas);
        }
    }

    private static class TintInfo {
        public ColorStateList mTintList;
        public PorterDuff.Mode mTintMode;
        public boolean mHasTintMode;
        public boolean mHasTintList;
    }

    private static class ForegroundInfo {
        public Drawable mDrawable;
        public TintInfo mTintInfo;
        public int mGravity = Gravity.FILL;
        // Not public API, so always true for apps.
        //public boolean mInsidePadding = true;
        // Cannot reliably track, so always true.
        //public boolean mBoundsChanged = true;
        public final Rect mSelfBounds = new Rect();
        public final Rect mOverlayBounds = new Rect();
    }
}
