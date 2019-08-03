/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foreground;

import android.annotation.SuppressLint;
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

class ForegroundHelper {

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
    private final Delegate mDelegate;

    private boolean mHasFrameworkForegroundSupport;

    @Nullable
    private ForegroundInfo mForegroundInfo;

    public ForegroundHelper(@NonNull Delegate delegate) {
        mDelegate = delegate;
    }

    public void init(@NonNull Context context, @Nullable AttributeSet attrs,
                     @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {

        // @see View#View(android.content.Context, android.util.AttributeSet, int, int)
        mHasFrameworkForegroundSupport = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M)
                || mDelegate.getView() instanceof FrameLayout;

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, STYLEABLE,
                defStyleAttr, defStyleRes);
        setForeground(a.getDrawable(STYLEABLE_ANDROID_FOREGROUND));
        setForegroundGravity(a.getInt(STYLEABLE_ANDROID_FOREGROUND_GRAVITY, Gravity.NO_GRAVITY));
        setForegroundTintMode(MoreDrawableCompat.parseTintMode(a.getInt(
                STYLEABLE_ANDROID_FOREGROUND_TINT_MODE, -1), null));
        setForegroundTintList(a.getColorStateList(STYLEABLE_ANDROID_FOREGROUND_TINT));
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
        mDelegate.superOnVisibilityAggregated(isVisible);

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        Drawable fg = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (fg != null && isVisible != fg.isVisible()) {
            fg.setVisible(isVisible, false);
        }
    }

    public void draw(@NonNull Canvas canvas) {
        mDelegate.superDraw(canvas);

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        onDrawForeground(canvas);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        mDelegate.superOnRtlPropertiesChanged(layoutDirection);

        if (mHasFrameworkForegroundSupport) {
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
        if (mHasFrameworkForegroundSupport) {
            return mDelegate.superVerifyDrawable(who);
        }

        return mDelegate.superVerifyDrawable(who) || (mForegroundInfo != null
                && mForegroundInfo.mDrawable == who);
    }

    public void drawableStateChanged() {
        mDelegate.superDrawableStateChanged();

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        int[] state = mDelegate.getView().getDrawableState();
        boolean changed = false;
        Drawable fg = mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        if (changed) {
            mDelegate.getView().invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        mDelegate.superDrawableHotspotChanged(x, y);

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null) {
            mForegroundInfo.mDrawable.setHotspot(x, y);
        }
    }

    public void jumpDrawablesToCurrentState() {
        mDelegate.superJumpDrawablesToCurrentState();

        if (mHasFrameworkForegroundSupport) {
            return;
        }

        if (mForegroundInfo != null && mForegroundInfo.mDrawable != null) {
            mForegroundInfo.mDrawable.jumpToCurrentState();
        }
    }

    @Nullable
    @SuppressLint("NewApi")
    public Drawable getForeground() {
        if (mHasFrameworkForegroundSupport) {
            return mDelegate.superGetForeground();
        }

        return mForegroundInfo != null ? mForegroundInfo.mDrawable : null;
    }

    @SuppressLint("NewApi")
    public void setForeground(@Nullable Drawable foreground) {
        if (mHasFrameworkForegroundSupport) {
            mDelegate.superSetForeground(foreground);
            return;
        }

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
            if (mDelegate.getView().isAttachedToWindow()) {
                mForegroundInfo.mDrawable.setVisible(false, false);
            }
            mForegroundInfo.mDrawable.setCallback(null);
            mDelegate.getView().unscheduleDrawable(mForegroundInfo.mDrawable);
        }

        mForegroundInfo.mDrawable = foreground;
        //mForegroundInfo.mBoundsChanged = true;
        if (foreground != null) {
            DrawableCompat.setLayoutDirection(foreground,
                    mDelegate.getView().getLayoutDirection());
            if (foreground.isStateful()) {
                foreground.setState(mDelegate.getView().getDrawableState());
            }
            applyForegroundTint();
            if (mDelegate.getView().isAttachedToWindow()) {
                foreground.setVisible(mDelegate.getView().getWindowVisibility() == View.VISIBLE
                        && mDelegate.getView().isShown(), false);
            }
            // Set callback last, since the view may still be initializing.
            foreground.setCallback(mDelegate.getView());
        }
        mDelegate.getView().requestLayout();
        mDelegate.getView().invalidate();
    }

    @SuppressLint("NewApi")
    public int getForegroundGravity() {
        if (mHasFrameworkForegroundSupport) {
            return mDelegate.superGetForegroundGravity();
        }

        return mForegroundInfo != null ? mForegroundInfo.mGravity : Gravity.START | Gravity.TOP;
    }

    @SuppressLint("NewApi")
    public void setForegroundGravity(int gravity) {
        if (mHasFrameworkForegroundSupport) {
            mDelegate.superSetForegroundGravity(gravity);
            return;
        }

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
            mDelegate.getView().requestLayout();
        }
    }

    @SuppressLint("NewApi")
    public void setForegroundTintList(@Nullable ColorStateList tint) {
        if (mHasFrameworkForegroundSupport) {
            mDelegate.superSetForegroundTintList(tint);
            return;
        }

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
    @SuppressLint("NewApi")
    public ColorStateList getForegroundTintList() {
        if (mHasFrameworkForegroundSupport) {
            return mDelegate.superGetForegroundTintList();
        }

        return mForegroundInfo != null && mForegroundInfo.mTintInfo != null ?
                mForegroundInfo.mTintInfo.mTintList : null;
    }

    @SuppressLint("NewApi")
    public void setForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mHasFrameworkForegroundSupport) {
            mDelegate.superSetForegroundTintMode(tintMode);
            return;
        }

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
    @SuppressLint("NewApi")
    public PorterDuff.Mode getForegroundTintMode() {
        if (mHasFrameworkForegroundSupport) {
            return mDelegate.superGetForegroundTintMode();
        }

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
                    mForegroundInfo.mDrawable.setState(mDelegate.getView().getDrawableState());
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
            selfBounds.set(0, 0, mDelegate.getView().getWidth(), mDelegate.getView().getHeight());
            //} else {
            //    selfBounds.set(getPaddingLeft(), getPaddingTop(),
            //            getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
            //}

            int ld = mDelegate.getView().getLayoutDirection();
            Gravity.apply(mForegroundInfo.mGravity, foreground.getIntrinsicWidth(),
                    foreground.getIntrinsicHeight(), selfBounds, overlayBounds, ld);
            foreground.setBounds(overlayBounds);
            //}

            foreground.draw(canvas);
        }
    }

    public interface Delegate {
        @NonNull
        View getView();
        @RequiresApi(Build.VERSION_CODES.N)
        void superOnVisibilityAggregated(boolean isVisible);
        void superDraw(@NonNull Canvas canvas);
        void superOnRtlPropertiesChanged(int layoutDirection);
        boolean superVerifyDrawable(@NonNull Drawable who);
        void superDrawableStateChanged();
        void superDrawableHotspotChanged(float x, float y);
        void superJumpDrawablesToCurrentState();
        @Nullable
        @RequiresApi(Build.VERSION_CODES.M)
        Drawable superGetForeground();
        @RequiresApi(Build.VERSION_CODES.M)
        void superSetForeground(@Nullable Drawable foreground);
        @RequiresApi(Build.VERSION_CODES.M)
        int superGetForegroundGravity();
        @RequiresApi(Build.VERSION_CODES.M)
        void superSetForegroundGravity(int gravity);
        @RequiresApi(Build.VERSION_CODES.M)
        void superSetForegroundTintList(@Nullable ColorStateList tint);
        @Nullable
        @RequiresApi(Build.VERSION_CODES.M)
        ColorStateList superGetForegroundTintList();
        @RequiresApi(Build.VERSION_CODES.M)
        void superSetForegroundTintMode(@Nullable PorterDuff.Mode tintMode);
        @Nullable
        @RequiresApi(Build.VERSION_CODES.M)
        PorterDuff.Mode superGetForegroundTintMode();
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
